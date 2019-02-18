package com.l2kt.gameserver.model.entity

import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.logging.CLogger
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.extensions.toAllOnlinePlayers
import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.group.Party
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.Earthquake
import com.l2kt.gameserver.network.serverpackets.ExRedSky
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.network.serverpackets.UserInfo
import com.l2kt.gameserver.templates.StatsSet
import java.util.concurrent.ScheduledFuture

/**
 * One of these swords can drop from any mob. But only one instance of each sword can exist in the world. When a cursed sword drops, the world becomes red for several seconds, the ground shakes, and there's also an announcement as a system message that a cursed sword is found.<br></br>
 * <br></br>
 * The owner automatically becomes chaotic and their HP/CP/MP are fully restored.<br></br>
 * <br></br>
 * A cursed sword is equipped automatically when it's found, and the owner doesn't have an option to unequip it, to drop it or to destroy it. With a cursed sword you get some special skills.<br></br>
 * <br></br>
 * The cursed swords disappear after a certain period of time, and it doesn't matter how much time the owner spends online. This period of time is reduced if the owner kills another player, but the abilities of the sword increase. However, the owner needs to kill at least one player per day,
 * otherwise the sword disappears in 24 hours. There will be system messages about how much lifetime the sword has and when last murder was committed.<br></br>
 * <br></br>
 * If the owner dies, the sword either disappears or drops. When the sword is gone, the owner gains back their skills and characteristics go back to normal.
 */
class CursedWeapon(set: StatsSet) {

    val name: String = set.getString("name")
    val itemId: Int = set.getInteger("id")

    private var _item: ItemInstance? = null

    var playerId = 0
        private set
    var player: Player? = null

    // Skill id and max level. Max level is took from skillid (allow custom skills).
    val skillId: Int = set.getInteger("skillId")
    private val _skillMaxLevel: Int

    // Drop rate (when a mob is killed) and chance of dissapear (when a CW owner dies).
    private val _dropRate: Int = set.getInteger("dropRate")
    private val _dissapearChance: Int = set.getInteger("dissapearChance")

    // Overall duration (in hours) and hungry - used for daily task - duration (in hours)
    private val _duration: Int = set.getInteger("duration")
    val durationLost: Int = set.getInteger("durationLost")

    // Basic number used to calculate next number of needed victims for a stage (50% to 150% the given value).
    val stageKills: Int = set.getInteger("stageKills")

    var isDropped = false
        private set
    var isActivated = false
        private set

    private var _overallTimer: ScheduledFuture<*>? = null
    private var _dailyTimer: ScheduledFuture<*>? = null
    private var _dropTimer: ScheduledFuture<*>? = null

    var playerKarma = 0
        private set
    var playerPkKills = 0
        private set

    // Number of current killed, current stage of weapon (1 by default, max is _skillMaxLevel), and number of victims needed for next stage.
    var nbKills = 0
        private set
    var currentStage = 1
        private set
    var numberBeforeNextStage = 0
        private set

    // Hungry timer (in minutes) and overall end timer (in ms).
    var hungryTime = 0
        private set
    var endTime: Long = 0
        private set

    val duration: Long
        get() = _duration.toLong()

    val isActive: Boolean
        get() = isActivated || isDropped

    val timeLeft: Long
        get() = endTime - System.currentTimeMillis()

    val worldPosition: Location?
        get() {
            if (isActivated && player != null)
                return player!!.position

            return if (isDropped && _item != null) _item!!.position else null

        }

    init {

        _skillMaxLevel = SkillTable.getMaxLevel(skillId)

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(LOAD_CW).use { ps ->
                    ps.setInt(1, itemId)

                    ps.executeQuery().use { rs ->
                        while (rs.next()) {
                            playerId = rs.getInt("playerId")
                            playerKarma = rs.getInt("playerKarma")
                            playerPkKills = rs.getInt("playerPkKills")
                            nbKills = rs.getInt("nbKills")
                            currentStage = rs.getInt("currentStage")
                            numberBeforeNextStage = rs.getInt("numberBeforeNextStage")
                            hungryTime = rs.getInt("hungryTime")
                            endTime = rs.getLong("endTime")

                            reActivate(false)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't restore cursed weapons data.", e)
        }

    }

    fun setItem(item: ItemInstance) {
        _item = item
    }

    /**
     * This method is used to destroy a [CursedWeapon].<br></br>
     * It manages following states :
     *
     *  * <u>item on a online player</u> : drops the cursed weapon from inventory, and set back ancient pk/karma values.
     *  * <u>item on a offline player</u> : make SQL operations in order to drop item from database.
     *  * <u>item on ground</u> : destroys the item directly.
     *
     * For all cases, a message is broadcasted, and the different states are reinitialized.
     */
    fun endOfLife() {
        if (isActivated) {
            // Player is online ; unequip weapon && destroy it.
            if (player != null && player!!.isOnline) {
                LOGGER.info("{} is being removed online.", name)

                player!!.abortAttack()

                player!!.karma = playerKarma
                player!!.pkKills = playerPkKills
                player!!.cursedWeaponEquippedId = 0
                removeDemonicSkills()

                // Unequip && remove.
                player!!.useEquippableItem(_item!!, true)
                player!!.destroyItemByItemId("CW", itemId, 1, player, false)

                player!!.broadcastUserInfo()

                player!!.store()
            } else {
                LOGGER.info("{} is being removed offline.", name)

                try {
                    L2DatabaseFactory.connection.use { con ->
                        // Delete the item
                        con.prepareStatement(DELETE_ITEM).use { ps ->
                            ps.setInt(1, playerId)
                            ps.setInt(2, itemId)
                        }

                        // Restore the karma and PK kills.
                        con.prepareStatement(UPDATE_PLAYER).use { ps ->
                            ps.setInt(1, playerKarma)
                            ps.setInt(2, playerPkKills)
                            ps.setInt(3, playerId)
                        }
                    }
                } catch (e: Exception) {
                    LOGGER.error("Couldn't cleanup {} from offline player {}.", e, name, playerId)
                }

            }// Player is offline ; make only SQL operations.
        } else {
            // This CW is in the inventory of someone who has another cursed weapon equipped.
            if (player != null && player!!.inventory!!.getItemByItemId(itemId) != null) {
                player!!.destroyItemByItemId("CW", itemId, 1, player, false)
                LOGGER.info("{} has been assimilated.", name)
            } else if (_item != null) {
                _item!!.decayMe()
                LOGGER.info("{} has been removed from world.", name)
            }// This CW is on the ground.
        }

        // Drop tasks.
        cancelDailyTimer()
        cancelOverallTimer()
        cancelDropTimer()

        // Delete infos from table, if any.
        removeFromDb()

        // Inform all ppl.
        SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_DISAPPEARED).addItemName(itemId).toAllOnlinePlayers()

        // Reset state.
        player = null
        _item = null

        isActivated = false
        isDropped = false

        nbKills = 0
        currentStage = 1
        numberBeforeNextStage = 0

        hungryTime = 0
        endTime = 0

        playerId = 0
        playerKarma = 0
        playerPkKills = 0
    }

    private fun cancelDailyTimer() {
        if (_dailyTimer != null) {
            _dailyTimer!!.cancel(false)
            _dailyTimer = null
        }
    }

    private fun cancelOverallTimer() {
        if (_overallTimer != null) {
            _overallTimer!!.cancel(false)
            _overallTimer = null
        }
    }

    private fun cancelDropTimer() {
        if (_dropTimer != null) {
            _dropTimer!!.cancel(false)
            _dropTimer = null
        }
    }

    /**
     * This method is used to drop the [CursedWeapon] from its [Player] owner.<br></br>
     * It drops the item on ground, and reset player stats and skills. Finally it broadcasts a message to all online players.
     * @param killer : The creature who killed the cursed weapon owner.
     */
    private fun dropFromPlayer(killer: Creature) {
        player!!.abortAttack()

        // Prevent item from being removed by ItemsAutoDestroy.
        _item?.isDestroyProtected = true
        player!!.dropItem("DieDrop", _item, killer, true)

        isActivated = false
        isDropped = true

        player!!.karma = playerKarma
        player!!.pkKills = playerPkKills
        player!!.cursedWeaponEquippedId = 0
        removeDemonicSkills()

        // Cancel the daily timer. It will be reactivated when someone will pickup the weapon.
        cancelDailyTimer()

        // Activate the "1h dropped CW" timer.
        _dropTimer = ThreadPool.schedule(DropTimer(), 3600000L)

        // Reset current stage to 1.
        currentStage = 1

        // Drop infos from database.
        removeFromDb()

        // Broadcast a message to all online players.
        SystemMessage.getSystemMessage(SystemMessageId.S2_WAS_DROPPED_IN_THE_S1_REGION).addZoneName(player!!.position)
            .addItemName(itemId).toAllOnlinePlayers()
    }

    /**
     * This method is used to drop the [CursedWeapon] from a [Attackable] monster.<br></br>
     * It drops the item on ground, and broadcast earthquake && red sky animations. Finally it broadcasts a message to all online players.
     * @param attackable : The monster who dropped the cursed weapon.
     * @param player : The player who killed the monster.
     */
    private fun dropFromMob(attackable: Attackable, player: Player) {
        isActivated = false

        // Get position.
        val x = attackable.x + Rnd[-70, 70]
        val y = attackable.y + Rnd[-70, 70]
        val z = GeoEngine.getHeight(x, y, attackable.z).toInt()

        // Create item and drop it.
        _item = ItemInstance.create(itemId, 1, player, attackable)
        _item!!.isDestroyProtected = true
        _item!!.dropMe(attackable, x, y, z)

        // RedSky and Earthquake
        ExRedSky(10).toAllOnlinePlayers()
        Earthquake(x, y, z, 14, 3).toAllOnlinePlayers()

        isDropped = true

        // Broadcast a message to all online players.
        SystemMessage.getSystemMessage(SystemMessageId.S2_WAS_DROPPED_IN_THE_S1_REGION).addZoneName(player.position)
            .addItemName(itemId).toAllOnlinePlayers()
    }

    /**
     * Method used to send messages :<br></br>
     *
     *  * one is broadcasted to warn players than [CursedWeapon] owner is online.
     *  * the other shows left timer for the cursed weapon owner (either in hours or minutes).
     *
     */
    fun cursedOnLogin() {
        var msg = SystemMessage.getSystemMessage(SystemMessageId.S2_OWNER_HAS_LOGGED_INTO_THE_S1_REGION)
        msg.addZoneName(player!!.position)
        msg.addItemName(player!!.cursedWeaponEquippedId)
        msg.toAllOnlinePlayers()

        val timeLeft = (timeLeft / 60000).toInt()
        if (timeLeft > 60) {
            msg = SystemMessage.getSystemMessage(SystemMessageId.S2_HOUR_OF_USAGE_TIME_ARE_LEFT_FOR_S1)
            msg.addItemName(player!!.cursedWeaponEquippedId)
            msg.addNumber(Math.round((timeLeft / 60).toFloat()))
        } else {
            msg = SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTE_OF_USAGE_TIME_ARE_LEFT_FOR_S1)
            msg.addItemName(player!!.cursedWeaponEquippedId)
            msg.addNumber(timeLeft)
        }
        player!!.sendPacket(msg)
    }

    /**
     * Rebind the passive skill belonging to the [CursedWeapon] owner. Invoke this method if the weapon owner switches to a subclass.
     */
    fun giveDemonicSkills() {
        val skill = SkillTable.getInfo(skillId, currentStage)
        if (skill != null) {
            player!!.addSkill(skill, false)
            player!!.sendSkillList()
        }
    }

    private fun removeDemonicSkills() {
        player!!.removeSkill(skillId, false)
        player!!.sendSkillList()
    }

    /**
     * Reactivate the [CursedWeapon]. It can be either coming from a player login, or a GM command.
     * @param fromZero : if set to true, both _hungryTime and _endTime will be reseted to their default values.
     */
    fun reActivate(fromZero: Boolean) {
        if (fromZero) {
            hungryTime = durationLost * 60
            endTime = System.currentTimeMillis() + _duration * 3600000L

            _overallTimer = ThreadPool.scheduleAtFixedRate(OverallTimer(), 60000L, 60000L)
        } else {
            isActivated = true

            if (endTime - System.currentTimeMillis() <= 0)
                endOfLife()
            else {
                _dailyTimer = ThreadPool.scheduleAtFixedRate(DailyTimer(), 60000L, 60000L)
                _overallTimer = ThreadPool.scheduleAtFixedRate(OverallTimer(), 60000L, 60000L)
            }
        }
    }

    /**
     * Handles the drop rate of a [CursedWeapon]. If successful, launches the different associated tasks (end, overall and drop timers).
     * @param attackable : The monster who drops the cursed weapon.
     * @param player : The player who killed the monster.
     * @return true if the drop rate is a success.
     */
    fun checkDrop(attackable: Attackable, player: Player): Boolean {
        if (Rnd[1000000] < _dropRate) {
            // Drop the item.
            dropFromMob(attackable, player)

            // Start timers.
            endTime = System.currentTimeMillis() + _duration * 3600000L
            _overallTimer = ThreadPool.scheduleAtFixedRate(OverallTimer(), 60000L, 60000L)
            _dropTimer = ThreadPool.schedule(DropTimer(), 3600000L)

            return true
        }
        return false
    }

    /**
     * Activate the [CursedWeapon]. We refresh [Player] owner, store related infos, save references, activate cursed weapon skills, expell him from the party (if any).<br></br>
     * <br></br>
     * Finally it broadcasts a message to all online players.
     * @param player : The player who pickup the cursed weapon.
     * @param item : The item used as reference.
     */
    fun activate(player: Player, item: ItemInstance) {
        // if the player is mounted, attempt to unmount first and pick it if successful.
        if (player.isMounted && !player.dismount()) {
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(item.itemId))
            item.isDestroyProtected = true
            player.dropItem("InvDrop", item, null, true)
            return
        }

        isActivated = true

        // Hold player data.
        this.player = player
        playerId = this.player!!.objectId
        playerKarma = this.player!!.karma
        playerPkKills = this.player!!.pkKills

        _item = item

        // Generate a random number for next stage.
        numberBeforeNextStage = Rnd[Math.round(stageKills * 0.5).toInt(), Math.round(stageKills * 1.5).toInt()]

        // Renew hungry time.
        hungryTime = durationLost * 60

        // Activate the daily timer.
        _dailyTimer = ThreadPool.scheduleAtFixedRate(DailyTimer(), 60000L, 60000L)

        // Cancel the "1h dropped CW" timer.
        cancelDropTimer()

        // Save data on database.
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(INSERT_CW).use { ps ->
                    ps.setInt(1, itemId)
                    ps.setInt(2, playerId)
                    ps.setInt(3, playerKarma)
                    ps.setInt(4, playerPkKills)
                    ps.setInt(5, nbKills)
                    ps.setInt(6, currentStage)
                    ps.setInt(7, numberBeforeNextStage)
                    ps.setInt(8, hungryTime)
                    ps.setLong(9, endTime)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Failed to insert cursed weapon data.", e)
        }

        // Change player stats
        this.player!!.cursedWeaponEquippedId = itemId
        this.player!!.karma = 9999999
        this.player!!.pkKills = 0

        if (this.player!!.isInParty)
            this.player!!.party!!.removePartyMember(this.player, Party.MessageType.EXPELLED)

        // Disable active toggles
        for (effect in this.player!!.allEffects) {
            if (effect.skill.isToggle)
                effect.exit()
        }

        // Add CW skills
        giveDemonicSkills()

        // Equip the weapon
        this.player!!.useEquippableItem(_item!!, true)

        // Fully heal player
        this.player!!.setCurrentHpMp(this.player!!.maxHp.toDouble(), this.player!!.maxMp.toDouble())
        this.player!!.currentCp = this.player!!.maxCp.toDouble()

        // Refresh player stats
        this.player!!.broadcastUserInfo()

        // _player.broadcastPacket(new SocialAction(_player, 17));
        SystemMessage.getSystemMessage(SystemMessageId.THE_OWNER_OF_S2_HAS_APPEARED_IN_THE_S1_REGION)
            .addZoneName(this.player!!.position).addItemName(_item!!.itemId).toAllOnlinePlayers()
    }

    /**
     * Drop dynamic infos regarding [CursedWeapon] for the given itemId. Used in endOfLife() method.
     */
    private fun removeFromDb() {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(DELETE_CW).use { ps ->
                    ps.setInt(1, itemId)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Failed to remove cursed weapon data.", e)
        }

    }

    /**
     * This method checks if the [CursedWeapon] is dropped or simply dissapears.
     * @param killer : The killer of cursed weapon owner.
     */
    fun dropIt(killer: Creature) {
        // Remove it
        if (Rnd[100] <= _dissapearChance)
            endOfLife()
        else
            dropFromPlayer(killer)// Unequip & Drop
    }

    /**
     * Increase the number of kills. If actual counter reaches the number generated to reach next stage, than rank up the [CursedWeapon].
     */
    fun increaseKills() {
        if (player != null && player!!.isOnline) {
            nbKills++
            hungryTime = durationLost * 60

            player!!.pkKills = player!!.pkKills + 1
            player!!.sendPacket(UserInfo(player!!))

            // If current number of kills is >= to the given number, than rankUp the weapon.
            if (nbKills >= numberBeforeNextStage) {
                // Reset the number of kills to 0.
                nbKills = 0

                // Setup the new random number.
                numberBeforeNextStage = Rnd[Math.round(stageKills * 0.5).toInt(), Math.round(stageKills * 1.5).toInt()]

                // Rank up the CW.
                rankUp()
            }
        }
    }

    /**
     * This method is used to rank up a CW.
     */
    fun rankUp() {
        if (currentStage >= _skillMaxLevel)
            return

        // Rank up current stage.
        currentStage++

        // Reward skills for that CW.
        giveDemonicSkills()
    }

    fun goTo(player: Player?) {
        if (player == null)
            return

        // Go to player holding the weapon
        if (isActivated)
            player.teleToLocation(this.player!!.x, this.player!!.y, this.player!!.z, 0)
        else if (isDropped)
            player.teleToLocation(_item!!.x, _item!!.y, _item!!.z, 0)
        else
            player.sendMessage("$name isn't in the world.")// Go to item on the ground
    }

    private inner class DailyTimer : Runnable {
        // Internal timer to delay messages to the next hour, instead of every minute.
        private var _timer = 0

        override fun run() {
            hungryTime--
            _timer++

            if (hungryTime <= 0)
                endOfLife()
            else if (player != null && player!!.isOnline && _timer % 60 == 0) {
                val msg: SystemMessage
                val timeLeft = (timeLeft / 60000).toInt()
                if (timeLeft > 60) {
                    msg = SystemMessage.getSystemMessage(SystemMessageId.S2_HOUR_OF_USAGE_TIME_ARE_LEFT_FOR_S1)
                    msg.addItemName(player!!.cursedWeaponEquippedId)
                    msg.addNumber(Math.round((timeLeft / 60).toFloat()))
                } else {
                    msg = SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTE_OF_USAGE_TIME_ARE_LEFT_FOR_S1)
                    msg.addItemName(player!!.cursedWeaponEquippedId)
                    msg.addNumber(timeLeft)
                }
                player!!.sendPacket(msg)
            }
        }
    }

    private inner class OverallTimer : Runnable {

        override fun run() {
            // Overall timer is reached, ends the life of CW.
            if (System.currentTimeMillis() >= endTime)
                endOfLife()
            else {
                try {
                    L2DatabaseFactory.connection.use { con ->
                        con.prepareStatement(UPDATE_CW).use { ps ->
                            ps.setInt(1, nbKills)
                            ps.setInt(2, currentStage)
                            ps.setInt(3, numberBeforeNextStage)
                            ps.setInt(4, hungryTime)
                            ps.setLong(5, endTime)
                            ps.setInt(6, itemId)
                            ps.executeUpdate()
                        }
                    }
                } catch (e: Exception) {
                    LOGGER.error("Failed to update cursed weapon data.", e)
                }

            }// Save data.
        }
    }

    private inner class DropTimer : Runnable {

        override fun run() {
            if (isDropped)
                endOfLife()
        }
    }

    companion object {
        private val LOGGER = CLogger(CursedWeapon::class.java.name)

        private val LOAD_CW = "SELECT * FROM cursed_weapons WHERE itemId=?"
        private val DELETE_ITEM = "DELETE FROM items WHERE owner_id=? AND item_id=?"
        private val UPDATE_PLAYER = "UPDATE characters SET karma=?, pkkills=? WHERE obj_id=?"
        private val INSERT_CW =
            "INSERT INTO cursed_weapons (itemId, playerId, playerKarma, playerPkKills, nbKills, currentStage, numberBeforeNextStage, hungryTime, endTime) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
        private val DELETE_CW = "DELETE FROM cursed_weapons WHERE itemId = ?"
        private val UPDATE_CW =
            "UPDATE cursed_weapons SET nbKills=?, currentStage=?, numberBeforeNextStage=?, hungryTime=?, endTime=? WHERE itemId=?"
    }
}