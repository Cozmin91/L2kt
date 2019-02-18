package com.l2kt.gameserver.model.entity

import com.l2kt.Config
import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.extensions.toAllOnlinePlayers
import com.l2kt.gameserver.model.L2Spawn
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.ControlTower
import com.l2kt.gameserver.model.actor.instance.FlameTower
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.PlaySound
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.network.serverpackets.UserInfo
import com.l2kt.gameserver.scripting.Quest
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture
import java.util.logging.Level
import java.util.logging.Logger

class Siege(val castle: Castle) : Siegable {

    private val _registeredClans = ConcurrentHashMap<Clan, SiegeSide>()

    private val _controlTowers = ArrayList<ControlTower>()
    private val _flameTowers = ArrayList<FlameTower>()

    private val _destroyedTowers = ArrayList<Npc>()

    protected var _siegeEndDate: Calendar? = null

    protected var _siegeTask: ScheduledFuture<*>? = null

    private var _formerOwner: Clan? = null
    var status = SiegeStatus.REGISTRATION_OPENED
        private set

    private var _questEvents: MutableList<Quest> = mutableListOf()

    override val attackerClans: List<Clan>
        get() = _registeredClans.entries.filter { e -> e.value == SiegeSide.ATTACKER }.map{ entry -> entry.key }

    override val defenderClans: List<Clan>
        get() = _registeredClans.entries.filter { e -> e.value == SiegeSide.DEFENDER || e.value == SiegeSide.OWNER }.map{ entry -> entry.key }

    override val siegeDate: Calendar?
        get() = castle.siegeDate

    val registeredClans: MutableMap<Clan, SiegeSide>
        get() = _registeredClans

    val pendingClans: List<Clan>
        get() = _registeredClans.entries.filter { e -> e.value == SiegeSide.PENDING }.map{ entry -> entry.key }

    val isInProgress: Boolean
        get() = status == SiegeStatus.IN_PROGRESS

    val isRegistrationOver: Boolean
        get() = status != SiegeStatus.REGISTRATION_OPENED

    val isTimeRegistrationOver: Boolean
        get() = castle.isTimeRegistrationOver

    /**
     * @return siege registration end date, which always equals siege date minus one day.
     */
    val siegeRegistrationEndDate: Long
        get() = castle.siegeDate!!.timeInMillis - 86400000

    val controlTowerCount: Int
        get() = _controlTowers.stream().filter { lc -> lc.isActive }.count().toInt()

    val destroyedTowers: List<Npc>
        get() = _destroyedTowers

    enum class SiegeSide {
        OWNER,
        DEFENDER,
        ATTACKER,
        PENDING
    }

    enum class SiegeStatus {
        REGISTRATION_OPENED, // Equals canceled or end siege event.
        REGISTRATION_OVER,
        IN_PROGRESS // Equals siege start event.
    }

    init {

        // Add castle owner as defender (add owner first so that they are on the top of the defender list)
        if (this.castle.ownerId > 0) {
            val clan = ClanTable.getClan(castle.ownerId)
            if (clan != null)
                _registeredClans[clan] = SiegeSide.OWNER
        }

        // Feed _registeredClans.
        try {
            L2DatabaseFactory.connection.use { con ->
                val ps = con.prepareStatement(LOAD_SIEGE_CLAN)
                ps.setInt(1, this.castle.castleId)

                val rs = ps.executeQuery()
                while (rs.next()) {
                    val clan = ClanTable.getClan(rs.getInt("clan_id"))
                    if (clan != null)
                        _registeredClans[clan] = SiegeSide.valueOf(rs.getString("type"))
                }
                rs.close()
                ps.close()
            }
        } catch (e: Exception) {
            _log.log(Level.WARNING, "Exception: loadSiegeClan(): " + e.message, e)
        }

        startAutoTask()
    }

    override fun startSiege() {
        if (isInProgress)
            return

        if (attackerClans.isEmpty()) {
            val sm =
                SystemMessage.getSystemMessage(if (castle.ownerId <= 0) SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST else SystemMessageId.S1_SIEGE_WAS_CANCELED_BECAUSE_NO_CLANS_PARTICIPATED)
            sm.addString(castle.name)

            sm.toAllOnlinePlayers()
            saveCastleSiege(true)
            return
        }

        _formerOwner = ClanTable.getClan(castle.ownerId)

        changeStatus(SiegeStatus.IN_PROGRESS) // Flag so that same siege instance cannot be started again

        updatePlayerSiegeStateFlags(false)
        castle.siegeZone!!.banishForeigners(castle.ownerId)

        spawnControlTowers() // Spawn control towers
        spawnFlameTowers() // Spawn flame towers
        castle.closeDoors() // Close doors

        castle.spawnSiegeGuardsOrMercenaries()

        castle.siegeZone!!.isActive = true
        castle.siegeZone!!.updateZoneStatusForCharactersInside()

        _siegeEndDate = Calendar.getInstance()
        _siegeEndDate?.add(Calendar.MINUTE, Config.SIEGE_LENGTH)

        // Schedule a task to prepare auto siege end
        ThreadPool.schedule(EndSiegeTask(castle), 1000)

        SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_STARTED).addString(castle.name)
            .toAllOnlinePlayers()
        PlaySound("systemmsg_e.17").toAllOnlinePlayers()
    }

    override fun endSiege() {
        if (!isInProgress)
            return

        SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_ENDED).addString(castle.name)
            .toAllOnlinePlayers()
        PlaySound("systemmsg_e.18").toAllOnlinePlayers()

        if (castle.ownerId > 0) {
            val clan = ClanTable.getClan(castle.ownerId)
            SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_VICTORIOUS_OVER_S2_S_SIEGE).addString(clan!!.name)
                .addString(castle.name).toAllOnlinePlayers()

            // An initial clan was holding the castle and is different of current owner.
            if (_formerOwner != null && clan != _formerOwner) {
                // Delete circlets and crown's leader for initial castle's owner (if one was existing)
                castle.checkItemsForClan(_formerOwner!!)

                // Refresh hero diaries.
                for (member in clan.members) {
                    val player = member.playerInstance
                    if (player != null && player.isNoble)
                        Hero.setCastleTaken(player.objectId, castle.castleId)
                }
            }
        } else
            SystemMessage.getSystemMessage(SystemMessageId.SIEGE_S1_DRAW).addString(castle.name).toAllOnlinePlayers()

        // Cleanup clans kills/deaths counters, cleanup flag.
        for (clan in _registeredClans.keys) {
            clan.siegeKills = 0
            clan.siegeDeaths = 0
            clan.flag = null
        }

        // Refresh reputation points towards siege end.
        updateClansReputation()

        // Teleport all non-owning castle players on second closest town.
        castle.siegeZone!!.banishForeigners(castle.ownerId)

        // Clear all flags.
        updatePlayerSiegeStateFlags(true)

        // Save castle specific data.
        saveCastleSiege(true)

        // Clear registered clans.
        clearAllClans()

        // Remove all towers from this castle.
        removeTowers()

        // Despawn guards or mercenaries.
        castle.despawnSiegeGuardsOrMercenaries()

        // Respawn/repair castle doors.
        castle.spawnDoors(false)

        castle.siegeZone!!.isActive = false
        castle.siegeZone!!.updateZoneStatusForCharactersInside()
    }

    override fun checkSide(clan: Clan?, type: SiegeSide): Boolean {
        return clan != null && _registeredClans[clan] == type
    }

    override fun checkSides(clan: Clan?, vararg types: SiegeSide): Boolean {
        return clan != null && types.contains(_registeredClans[clan])
    }

    override fun checkSides(clan: Clan?): Boolean {
        return clan != null && _registeredClans.containsKey(clan)
    }

    override fun getFlag(clan: Clan?): Npc? {
        return if (checkSide(clan, SiegeSide.ATTACKER)) clan?.flag else null
    }

    /**
     * Update clan reputation points over siege end, as following :
     *
     *  * The former clan failed to defend the castle : 1000 points for new owner, -1000 for former clan.
     *  * The former clan successfully defended the castle, ending in a draw : 500 points for former clan.
     *  * No former clan, which means players successfully attacked over NPCs : 1000 points for new owner.
     *
     */
    fun updateClansReputation() {
        val owner = ClanTable.getClan(castle.ownerId)
        if (_formerOwner != null) {
            // Defenders fail
            if (_formerOwner != owner) {
                _formerOwner!!.takeReputationScore(1000)
                _formerOwner!!.broadcastToOnlineMembers(
                    SystemMessage.getSystemMessage(SystemMessageId.CLAN_WAS_DEFEATED_IN_SIEGE_AND_LOST_S1_REPUTATION_POINTS).addNumber(
                        1000
                    )
                )

                // Attackers succeed over defenders
                if (owner != null) {
                    owner.addReputationScore(1000)
                    owner.broadcastToOnlineMembers(
                        SystemMessage.getSystemMessage(SystemMessageId.CLAN_VICTORIOUS_IN_SIEGE_AND_GAINED_S1_REPUTATION_POINTS).addNumber(
                            1000
                        )
                    )
                }
            } else {
                _formerOwner!!.addReputationScore(500)
                _formerOwner!!.broadcastToOnlineMembers(
                    SystemMessage.getSystemMessage(SystemMessageId.CLAN_VICTORIOUS_IN_SIEGE_AND_GAINED_S1_REPUTATION_POINTS).addNumber(
                        500
                    )
                )
            }// Draw
        } else if (owner != null) {
            owner.addReputationScore(1000)
            owner.broadcastToOnlineMembers(
                SystemMessage.getSystemMessage(SystemMessageId.CLAN_VICTORIOUS_IN_SIEGE_AND_GAINED_S1_REPUTATION_POINTS).addNumber(
                    1000
                )
            )
        }// Attackers win over NPCs
    }

    /**
     * This method is used to switch all SiegeClanType from one type to another.
     * @param clan
     * @param newState
     */
    private fun switchSide(clan: Clan?, newState: SiegeSide) {
        if(clan != null) _registeredClans[clan] = newState
    }

    /**
     * This method is used to switch all SiegeClanType from one type to another.
     * @param previousStates
     * @param newState
     */
    private fun switchSides(newState: SiegeSide, vararg previousStates: SiegeSide) {
        for (entry in _registeredClans.entries) {
            if(previousStates.contains(entry.value))
                entry.setValue(newState)
        }
    }

    fun getSide(clan: Clan): SiegeSide? {
        return _registeredClans[clan]
    }

    /**
     * Check if both clans are registered as opponent.
     * @param formerClan : The first clan to check.
     * @param targetClan : The second clan to check.
     * @return true if one side is attacker/defender and other side is defender/attacker and false if one of clan isn't registered or previous statement didn't match.
     */
    fun isOnOppositeSide(formerClan: Clan, targetClan: Clan): Boolean {
        val formerSide = _registeredClans[formerClan]
        val targetSide = _registeredClans[targetClan]

        // Clan isn't even registered ; return false.
        return if (formerSide == null || targetSide == null) false else targetSide == SiegeSide.ATTACKER && (formerSide == SiegeSide.OWNER || formerSide == SiegeSide.DEFENDER || formerSide == SiegeSide.PENDING) || formerSide == SiegeSide.ATTACKER && (targetSide == SiegeSide.OWNER || targetSide == SiegeSide.DEFENDER || targetSide == SiegeSide.PENDING)

        // One side is owner, pending or defender and the other is attacker ; or vice-versa.
    }

    /**
     * When control of castle changed during siege.
     */
    fun midVictory() {
        if (!isInProgress)
            return

        castle.despawnSiegeGuardsOrMercenaries()

        if (castle.ownerId <= 0)
            return

        val attackers = attackerClans
        val defenders = defenderClans

        val castleOwner = ClanTable.getClan(castle.ownerId)

        // No defending clans and only one attacker, end siege.
        if (defenders.isEmpty() && attackers.size == 1) {
            switchSide(castleOwner, SiegeSide.OWNER)
            endSiege()
            return
        }

        val allyId = castleOwner!!.allyId

        // No defending clans and all attackers are part of the newly named castle owner alliance.
        if (defenders.isEmpty() && allyId != 0) {
            var allInSameAlliance = true
            for (clan in attackers) {
                if (clan.allyId != allyId) {
                    allInSameAlliance = false
                    break
                }
            }

            if (allInSameAlliance) {
                switchSide(castleOwner, SiegeSide.OWNER)
                endSiege()
                return
            }
        }

        // All defenders and owner become attackers.
        switchSides(SiegeSide.ATTACKER, SiegeSide.DEFENDER, SiegeSide.OWNER)

        // Newly named castle owner is setted.
        switchSide(castleOwner, SiegeSide.OWNER)

        // Define newly named castle owner registered allies as defenders.
        if (allyId != 0) {
            for (clan in attackers) {
                if (clan.allyId == allyId)
                    switchSide(clan, SiegeSide.DEFENDER)
            }
        }
        castle.siegeZone!!.banishForeigners(castle.ownerId)

        // Removes defenders' flags.
        for (clan in defenders)
            clan.flag = null

        castle.removeDoorUpgrade() // Remove all castle doors upgrades.
        castle.removeTrapUpgrade() // Remove all castle traps upgrades.
        castle.spawnDoors(true) // Respawn door to castle but make them weaker (50% hp).

        removeTowers() // Remove all towers from this castle.

        spawnControlTowers() // Each new siege midvictory CT are completely respawned.
        spawnFlameTowers()

        updatePlayerSiegeStateFlags(false)
    }

    /**
     * Broadcast a [SystemMessage] to defenders (or attackers if parameter is set).
     * @param message : The SystemMessage of the message to send to player
     * @param bothSides : If true, broadcast it too to attackers clans.
     */
    fun announceToPlayers(message: SystemMessage, bothSides: Boolean) {
        for (clan in defenderClans)
            clan.broadcastToOnlineMembers(message)

        if (bothSides) {
            for (clan in attackerClans)
                clan.broadcastToOnlineMembers(message)
        }
    }

    fun updatePlayerSiegeStateFlags(clear: Boolean) {
        for (clan in attackerClans) {
            for (member in clan.onlineMembers) {
                if (clear) {
                    member.siegeState = 0.toByte()
                    member.isInSiege = false
                } else {
                    member.siegeState = 1.toByte()
                    if (checkIfInZone(member))
                        member.isInSiege = true
                }
                member.sendPacket(UserInfo(member))
                member.broadcastRelationsChanges()
            }
        }

        for (clan in defenderClans) {
            for (member in clan.onlineMembers) {
                if (clear) {
                    member.siegeState = 0.toByte()
                    member.isInSiege = false
                } else {
                    member.siegeState = 2.toByte()
                    if (checkIfInZone(member))
                        member.isInSiege = true
                }
                member.sendPacket(UserInfo(member))
                member.broadcastRelationsChanges()
            }
        }
    }

    /**
     * Check if an object is inside an area using his location.
     * @param object The Object to use positions.
     * @return true if object is inside the zone
     */
    fun checkIfInZone(`object`: WorldObject): Boolean {
        return checkIfInZone(`object`.x, `object`.y, `object`.z)
    }

    /**
     * @param x
     * @param y
     * @param z
     * @return true if object is inside the zone
     */
    fun checkIfInZone(x: Int, y: Int, z: Int): Boolean {
        return isInProgress && castle.checkIfInZone(x, y, z) // Castle zone during siege
    }

    /** Clear all registered siege clans from database for castle  */
    fun clearAllClans() {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(CLEAR_SIEGE_CLANS).use { ps ->
                    ps.setInt(1, castle.castleId)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            _log.log(Level.SEVERE, "Error clearing registered clans.", e)
        }

        _registeredClans.clear()

        // Add back the owner after cleaning the map.
        if (castle.ownerId > 0) {
            val clan = ClanTable.getClan(castle.ownerId)
            if (clan != null)
                _registeredClans[clan] = SiegeSide.OWNER
        }
    }

    /** Clear all siege clans waiting for approval from database for castle  */
    protected fun clearPendingClans() {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(CLEAR_PENDING_CLANS).use { ps ->
                    ps.setInt(1, castle.castleId)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            _log.log(Level.SEVERE, "Error clearing pending clans.", e)
        }

        _registeredClans.entries.removeIf { e -> e.value == SiegeSide.PENDING }
    }

    /**
     * Register clan as attacker
     * @param player : The player trying to register
     */
    fun registerAttacker(player: Player) {
        if (player.clan == null)
            return

        var allyId = 0
        if (castle.ownerId != 0)
            allyId = ClanTable.getClan(castle.ownerId)!!.allyId

        // If the castle owning clan got an alliance
        if (allyId != 0) {
            // Same alliance can't be attacked
            if (player.clan!!.allyId == allyId) {
                player.sendPacket(SystemMessageId.CANNOT_ATTACK_ALLIANCE_CASTLE)
                return
            }
        }

        // Can't register as attacker if at least one allied clan is registered as defender
        if (allyIsRegisteredOnOppositeSide(player.clan!!, true))
            player.sendPacket(SystemMessageId.CANT_ACCEPT_ALLY_ENEMY_FOR_SIEGE)
        else if (checkIfCanRegister(player, SiegeSide.ATTACKER))
            registerClan(player.clan!!, SiegeSide.ATTACKER)// Save to database
    }

    /**
     * Register clan as defender.
     * @param player : The player trying to register
     */
    fun registerDefender(player: Player) {
        if (player.clan == null)
            return

        // Castle owned by NPC is considered as full side
        if (castle.ownerId <= 0)
            player.sendPacket(SystemMessageId.DEFENDER_SIDE_FULL)
        else if (allyIsRegisteredOnOppositeSide(player.clan!!, false))
            player.sendPacket(SystemMessageId.CANT_ACCEPT_ALLY_ENEMY_FOR_SIEGE)
        else if (checkIfCanRegister(player, SiegeSide.PENDING))
            registerClan(player.clan!!, SiegeSide.PENDING)// Save to database
        // Can't register as defender if at least one allied clan is registered as attacker
    }

    /**
     * Verify if allies are registered on different list than the actual player's choice. Let's say clan A and clan B are in same alliance. If clan A wants to attack a castle, clan B mustn't be on defenders' list. The contrary is right too : you can't defend if one ally is on attackers' list.
     * @param clan : The clan used for alliance existence checks.
     * @param attacker : A boolean used to know if this check is used for attackers or defenders.
     * @return true if one clan of the alliance is registered in other side.
     */
    private fun allyIsRegisteredOnOppositeSide(clan: Clan, attacker: Boolean): Boolean {
        // Check if player's clan got an alliance ; if not, skip the check
        val allyId = clan.allyId
        if (allyId != 0) {
            // Verify through the clans list for existing clans
            for (alliedClan in ClanTable.clans) {
                // If a clan with same allyId is found (so, same alliance)
                if (alliedClan.allyId == allyId) {
                    // Skip player's clan from the check
                    if (alliedClan.clanId == clan.clanId)
                        continue

                    // If the check is made for attackers' list
                    if (attacker) {
                        // Check if the allied clan is on defender / defender waiting lists
                        if (checkSides(alliedClan, SiegeSide.DEFENDER, SiegeSide.OWNER, SiegeSide.PENDING))
                            return true
                    } else {
                        // Check if the allied clan is on attacker list
                        if (checkSides(alliedClan, SiegeSide.ATTACKER))
                            return true
                    }
                }
            }
        }
        return false
    }

    /**
     * Remove clan from siege. Drop it from _registeredClans and database. Castle owner can't be dropped.
     * @param clan : The clan to check.
     */
    fun unregisterClan(clan: Clan?) {
        // Check if clan parameter is ok, avoid to drop castle owner, then remove if possible. If it couldn't be removed, return.
        if (clan == null || clan.castleId == castle.castleId || _registeredClans.remove(clan) == null)
            return

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(CLEAR_SIEGE_CLAN).use { ps ->
                    ps.setInt(1, castle.castleId)
                    ps.setInt(2, clan.clanId)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            _log.log(Level.SEVERE, "Error unregistering clan.", e)
        }

    }

    /**
     * This method allows to :
     *
     *  * Check if the siege time is deprecated, and recalculate otherwise.
     *  * Schedule start siege (it's in an else because saveCastleSiege() already affect it).
     *
     */
    private fun startAutoTask() {
        if (castle.siegeDate!!.timeInMillis < Calendar.getInstance().timeInMillis)
            saveCastleSiege(false)
        else {
            if (_siegeTask != null)
                _siegeTask!!.cancel(false)

            _siegeTask = ThreadPool.schedule(SiegeTask(castle), 1000)
        }
    }

    /**
     * @param player : The player trying to register.
     * @param type : The SiegeSide to test.
     * @return true if the player can register.
     */
    private fun checkIfCanRegister(player: Player, type: SiegeSide): Boolean {
        val sm: SystemMessage

        if (isRegistrationOver)
            sm = SystemMessage.getSystemMessage(SystemMessageId.DEADLINE_FOR_SIEGE_S1_PASSED).addString(castle.name)
        else if (isInProgress)
            sm = SystemMessage.getSystemMessage(SystemMessageId.NOT_SIEGE_REGISTRATION_TIME2)
        else if (player.clan == null || player.clan!!.level < Config.MINIMUM_CLAN_LEVEL)
            sm = SystemMessage.getSystemMessage(SystemMessageId.ONLY_CLAN_LEVEL_4_ABOVE_MAY_SIEGE)
        else if (player.clan!!.hasCastle())
            sm =
                    if (player.clan!!.clanId == castle.ownerId) SystemMessage.getSystemMessage(SystemMessageId.CLAN_THAT_OWNS_CASTLE_IS_AUTOMATICALLY_REGISTERED_DEFENDING) else SystemMessage.getSystemMessage(
                        SystemMessageId.CLAN_THAT_OWNS_CASTLE_CANNOT_PARTICIPATE_OTHER_SIEGE
                    )
        else if (player.clan!!.isRegisteredOnSiege)
            sm = SystemMessage.getSystemMessage(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE)
        else if (checkIfAlreadyRegisteredForSameDay(player.clan!!))
            sm =
                    SystemMessage.getSystemMessage(SystemMessageId.APPLICATION_DENIED_BECAUSE_ALREADY_SUBMITTED_A_REQUEST_FOR_ANOTHER_SIEGE_BATTLE)
        else if (type == SiegeSide.ATTACKER && attackerClans.size >= Config.MAX_ATTACKERS_NUMBER)
            sm = SystemMessage.getSystemMessage(SystemMessageId.ATTACKER_SIDE_FULL)
        else if ((type == SiegeSide.DEFENDER || type == SiegeSide.PENDING || type == SiegeSide.OWNER) && defenderClans.size + pendingClans.size >= Config.MAX_DEFENDERS_NUMBER)
            sm = SystemMessage.getSystemMessage(SystemMessageId.DEFENDER_SIDE_FULL)
        else
            return true

        player.sendPacket(sm)
        return false
    }

    /**
     * @param clan The L2Clan of the player trying to register
     * @return true if the clan has already registered to a siege for the same day.
     */
    fun checkIfAlreadyRegisteredForSameDay(clan: Clan): Boolean {
        for (castle in CastleManager.castles) {
            val siege = castle.siege
            if (siege === this)
                continue

            if (siege.siegeDate?.get(Calendar.DAY_OF_WEEK) == siegeDate?.get(Calendar.DAY_OF_WEEK) && siege.checkSides(
                    clan
                )
            )
                return true
        }
        return false
    }

    /** Remove all spawned towers.  */
    private fun removeTowers() {
        for (ct in _flameTowers)
            ct.deleteMe()

        for (ct in _controlTowers)
            ct.deleteMe()

        for (ct in _destroyedTowers)
            ct.deleteMe()

        _flameTowers.clear()
        _controlTowers.clear()
        _destroyedTowers.clear()
    }

    /**
     * Save castle siege related to database.
     * @param launchTask : if true, launch the start siege task.
     */
    private fun saveCastleSiege(launchTask: Boolean) {
        // Set the next siege date in 2 weeks from now.
        setNextSiegeDate()

        // You can edit time anew.
        castle.isTimeRegistrationOver = false

        // Save the new date.
        saveSiegeDate()

        // Prepare start siege task.
        if (launchTask)
            startAutoTask()

        _log.info("New date for " + castle.name + " siege: " + castle.siegeDate!!.time)
    }

    /**
     * Save siege date to database.
     */
    private fun saveSiegeDate() {
        if (_siegeTask != null) {
            _siegeTask!!.cancel(true)
            _siegeTask = ThreadPool.schedule(SiegeTask(castle), 1000)
        }

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(UPDATE_SIEGE_INFOS).use { ps ->
                    ps.setLong(1, siegeDate!!.timeInMillis)
                    ps.setString(2, isTimeRegistrationOver.toString())
                    ps.setInt(3, castle.castleId)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            _log.log(Level.SEVERE, "Error saving siege date.", e)
        }

    }

    /**
     * Save registration to database.
     * @param clan : The L2Clan of player.
     * @param type
     */
    fun registerClan(clan: Clan, type: SiegeSide) {
        if (clan.hasCastle())
            return

        when (type) {
            Siege.SiegeSide.DEFENDER, Siege.SiegeSide.PENDING, Siege.SiegeSide.OWNER -> if (defenderClans.size + pendingClans.size >= Config.MAX_DEFENDERS_NUMBER)
                return

            else -> if (attackerClans.size >= Config.MAX_ATTACKERS_NUMBER)
                return
        }

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(ADD_OR_UPDATE_SIEGE_CLAN).use { ps ->
                    ps.setInt(1, clan.clanId)
                    ps.setInt(2, castle.castleId)
                    ps.setString(3, type.toString())
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            _log.log(Level.SEVERE, "Error registering clan on siege.", e)
        }

        _registeredClans[clan] = type
    }

    /**
     * Set the date for the next siege.
     */
    private fun setNextSiegeDate() {
        val siegeDate = castle.siegeDate
        if (siegeDate!!.timeInMillis < System.currentTimeMillis())
            siegeDate.timeInMillis = System.currentTimeMillis()

        when (castle.castleId) {
            3, 4, 6, 7 -> siegeDate.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

            else -> siegeDate.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
        }

        // Set next siege date if siege has passed ; add 14 days (2 weeks).
        siegeDate.add(Calendar.WEEK_OF_YEAR, 2)

        // Set default hour to 18:00. This can be changed - only once - by the castle leader via the chamberlain.
        siegeDate.set(Calendar.HOUR_OF_DAY, 18)
        siegeDate.set(Calendar.MINUTE, 0)
        siegeDate.set(Calendar.SECOND, 0)
        siegeDate.set(Calendar.MILLISECOND, 0)

        // Send message and allow registration for next siege.
        SystemMessage.getSystemMessage(SystemMessageId.S1_ANNOUNCED_SIEGE_TIME).addString(castle.name)
            .toAllOnlinePlayers()
        changeStatus(SiegeStatus.REGISTRATION_OPENED)
    }

    /**
     * Spawn control towers.
     */
    private fun spawnControlTowers() {
        for (ts in castle.controlTowers) {
            try {
                val spawn = L2Spawn(NpcData.getTemplate(ts.id))
                spawn.loc = ts

                val tower = spawn.doSpawn(false) as ControlTower?
                tower!!.castle = castle

                _controlTowers.add(tower)
            } catch (e: Exception) {
                _log.warning(javaClass.name + ": Cannot spawn control tower! " + e)
            }

        }
    }

    /**
     * Spawn flame towers.
     */
    private fun spawnFlameTowers() {
        for (ts in castle.flameTowers) {
            try {
                val spawn = L2Spawn(NpcData.getTemplate(ts.id))
                spawn.loc = ts

                val tower = spawn.doSpawn(false) as FlameTower?
                tower!!.castle = castle
                tower.setUpgradeLevel(ts.upgradeLevel)
                tower.setZoneList(ts.zoneList)

                _flameTowers.add(tower)
            } catch (e: Exception) {
                _log.warning(javaClass.name + ": Cannot spawn flame tower! " + e)
            }

        }
    }

    fun endTimeRegistration(automatic: Boolean) {
        castle.isTimeRegistrationOver = true
        if (!automatic)
            saveSiegeDate()
    }

    inner class EndSiegeTask(private val _castle: Castle) : Runnable {

        override fun run() {
            if (!isInProgress)
                return

            val timeRemaining = _siegeEndDate!!.timeInMillis - Calendar.getInstance().timeInMillis
            if (timeRemaining > 3600000) {
                announceToPlayers(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_HOURS_UNTIL_SIEGE_CONCLUSION).addNumber(
                        2
                    ), true
                )
                ThreadPool.schedule(EndSiegeTask(_castle), timeRemaining - 3600000)
            } else if (timeRemaining in 600001..3600000) {
                announceToPlayers(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_SIEGE_CONCLUSION).addNumber(
                        Math.round((timeRemaining / 60000).toFloat())
                    ), true
                )
                ThreadPool.schedule(EndSiegeTask(_castle), timeRemaining - 600000)
            } else if (timeRemaining in 300001..600000) {
                announceToPlayers(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_SIEGE_CONCLUSION).addNumber(
                        Math.round((timeRemaining / 60000).toFloat())
                    ), true
                )
                ThreadPool.schedule(EndSiegeTask(_castle), timeRemaining - 300000)
            } else if (timeRemaining in 10001..300000) {
                announceToPlayers(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_SIEGE_CONCLUSION).addNumber(
                        Math.round((timeRemaining / 60000).toFloat())
                    ), true
                )
                ThreadPool.schedule(EndSiegeTask(_castle), timeRemaining - 10000)
            } else if (timeRemaining in 1..10000) {
                announceToPlayers(
                    SystemMessage.getSystemMessage(SystemMessageId.CASTLE_SIEGE_S1_SECONDS_LEFT).addNumber(
                        Math.round((timeRemaining / 1000).toFloat())
                    ), true
                )
                ThreadPool.schedule(EndSiegeTask(_castle), timeRemaining)
            } else
                _castle.siege.endSiege()
        }
    }

    private inner class SiegeTask(private val _castle: Castle) : Runnable {

        override fun run() {
            _siegeTask!!.cancel(false)
            if (isInProgress)
                return

            if (!isTimeRegistrationOver) {
                val regTimeRemaining = siegeRegistrationEndDate - Calendar.getInstance().timeInMillis
                if (regTimeRemaining > 0) {
                    _siegeTask = ThreadPool.schedule(SiegeTask(_castle), regTimeRemaining)
                    return
                }

                endTimeRegistration(true)
            }

            val timeRemaining = siegeDate!!.timeInMillis - Calendar.getInstance().timeInMillis

            if (timeRemaining > 86400000)
                _siegeTask = ThreadPool.schedule(SiegeTask(_castle), timeRemaining - 86400000)
            else if (timeRemaining in 13600001..86400000) {
                SystemMessage.getSystemMessage(SystemMessageId.REGISTRATION_TERM_FOR_S1_ENDED).addString(castle.name)
                    .toAllOnlinePlayers()
                changeStatus(SiegeStatus.REGISTRATION_OVER)
                clearPendingClans()
                _siegeTask = ThreadPool.schedule(SiegeTask(_castle), timeRemaining - 13600000)
            } else if (timeRemaining in 600001..13600000)
                _siegeTask = ThreadPool.schedule(SiegeTask(_castle), timeRemaining - 600000)
            else if (timeRemaining in 300001..600000)
                _siegeTask = ThreadPool.schedule(SiegeTask(_castle), timeRemaining - 300000)
            else if (timeRemaining in 10001..300000)
                _siegeTask = ThreadPool.schedule(SiegeTask(_castle), timeRemaining - 10000)
            else if (timeRemaining in 1..10000)
                _siegeTask = ThreadPool.schedule(SiegeTask(_castle), timeRemaining)
            else
                _castle.siege.startSiege()
        }
    }

    fun addQuestEvent(quest: Quest) {
        if (_questEvents.isEmpty())
            _questEvents = ArrayList(3)

        _questEvents.add(quest)
    }

    protected fun changeStatus(status: SiegeStatus) {
        this.status = status

        for (quest in _questEvents)
            quest.onSiegeEvent()
    }

    companion object {

        protected val _log = Logger.getLogger(Siege::class.java.name)

        private const val LOAD_SIEGE_CLAN = "SELECT clan_id,type FROM siege_clans WHERE castle_id=?"

        private const val CLEAR_SIEGE_CLANS = "DELETE FROM siege_clans WHERE castle_id=?"
        private const val CLEAR_PENDING_CLANS = "DELETE FROM siege_clans WHERE castle_id=? AND type='PENDING'"

        private const val CLEAR_SIEGE_CLAN = "DELETE FROM siege_clans WHERE castle_id=? AND clan_id=?"

        private const val UPDATE_SIEGE_INFOS = "UPDATE castle SET siegeDate=?, regTimeOver=? WHERE id=?"

        private const val ADD_OR_UPDATE_SIEGE_CLAN =
            "INSERT INTO siege_clans (clan_id,castle_id,type) VALUES (?,?,?) ON DUPLICATE KEY UPDATE type=VALUES(type)"
    }
}