package com.l2kt.gameserver.model.group

import com.l2kt.Config
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.math.MathUtil
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.manager.DimensionalRiftManager
import com.l2kt.gameserver.data.manager.DuelManager
import com.l2kt.gameserver.instancemanager.SevenSignsFestival
import com.l2kt.gameserver.model.BlockList
import com.l2kt.gameserver.model.RewardInfo
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.instance.Servitor
import com.l2kt.gameserver.model.holder.IntIntHolder
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.partymatching.PartyMatchRoomList
import com.l2kt.gameserver.model.rift.DimensionalRift
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.*
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Future

class Party(leader: Player, target: Player, val lootRule: LootRule) : AbstractGroup(leader) {

    private val _members = CopyOnWriteArrayList<Player>()

    /**
     * Check if another player can start invitation process.
     * @return boolean if party waits for invitation respond.
     */
    /**
     * Set invitation process flag and store time for expiration happens when player join or decline to join.
     * @param val : set the invitation process flag to that value.
     */
    var pendingInvitation: Boolean = false
        set(`val`) {
            field = `val`
            _pendingInviteTimeout = System.currentTimeMillis() + Player.REQUEST_TIMEOUT * 1000
        }
    private var _pendingInviteTimeout: Long = 0
    private var _itemLastLoot: Int = 0

    var commandChannel: CommandChannel? = null
    var dimensionalRift: DimensionalRift? = null

    private var _positionBroadcastTask: Future<*>? = null
    protected var _positionPacket: PartyMemberPosition? = null

    override val members: List<Player>
        get() = _members

    override val membersCount: Int
        get() = _members.size

    /**
     * Check if player invitation is expired.
     * @return boolean if time is expired.
     * @see Player.isRequestExpired
     */
    val isInvitationRequestExpired: Boolean
        get() = _pendingInviteTimeout <= System.currentTimeMillis()

    val isInCommandChannel: Boolean
        get() = commandChannel != null

    val isInDimensionalRift: Boolean
        get() = dimensionalRift != null

    enum class MessageType {
        EXPELLED,
        LEFT,
        NONE,
        DISCONNECTED
    }

    enum class LootRule private constructor(val messageId: SystemMessageId) {
        ITEM_LOOTER(SystemMessageId.LOOTING_FINDERS_KEEPERS),
        ITEM_RANDOM(SystemMessageId.LOOTING_RANDOM),
        ITEM_RANDOM_SPOIL(SystemMessageId.LOOTING_RANDOM_INCLUDE_SPOIL),
        ITEM_ORDER(SystemMessageId.LOOTING_BY_TURN),
        ITEM_ORDER_SPOIL(SystemMessageId.LOOTING_BY_TURN_INCLUDE_SPOIL);


        companion object {

            val VALUES = values()
        }
    }

    init {

        _members.add(leader)
        _members.add(target)

        leader.party = this
        target.party = this

        recalculateLevel()

        // Send new member party window for all members.
        target.sendPacket(PartySmallWindowAll(target, this))
        leader.sendPacket(PartySmallWindowAdd(target, this))

        // Send messages.
        target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_JOINED_S1_PARTY).addCharName(leader))
        leader.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_JOINED_PARTY).addCharName(target))

        // Update icons.
        for (member in _members) {
            member.updateEffectIcons(true)
            member.broadcastUserInfo()
        }

        _positionBroadcastTask = ThreadPool.scheduleAtFixedRate(
            PositionBroadcast(),
            (PARTY_POSITION_BROADCAST / 2).toLong(),
            PARTY_POSITION_BROADCAST.toLong()
        )
    }

    override fun equals(obj: Any?): Boolean {
        if (obj !is Party)
            return false

        return if (obj === this) true else isLeader(obj.leader!!)

    }

    override fun containsPlayer(player: WorldObject): Boolean {
        return _members.contains(player)
    }

    override fun broadcastPacket(packet: L2GameServerPacket) {
        for (member in _members)
            member.sendPacket(packet)
    }

    override fun broadcastCreatureSay(msg: CreatureSay, broadcaster: Player) {
        for (member in _members) {
            if (!BlockList.isBlocked(member, broadcaster))
                member.sendPacket(msg)
        }
    }

    override fun recalculateLevel() {
        var newLevel = 0
        for (member in _members) {
            if (member.level > newLevel)
                newLevel = member.level
        }
        level = newLevel
    }

    override fun disband() {
        // Cancel current rift session.
        DimensionalRiftManager.onPartyEdit(this)

        // Cancel party duel based on leader, as it will affect all players anyway.
        DuelManager.onPartyEdit(leader)

        // Delete the CommandChannel, or remove Party from it.
        if (commandChannel != null) {
            broadcastPacket(ExCloseMPCC.STATIC_PACKET)

            if (commandChannel!!.isLeader(leader!!))
                commandChannel!!.disband()
            else
                commandChannel!!.removeParty(this)
        }

        for (member in _members) {
            member.party = null
            member.sendPacket(PartySmallWindowDeleteAll.STATIC_PACKET)

            if (member.isFestivalParticipant)
                SevenSignsFestival.updateParticipants(member, this)

            if (member.fusionSkill != null)
                member.abortCast()

            for (character in member.getKnownType(Creature::class.java))
                if (character.fusionSkill != null && character.fusionSkill!!.target === member)
                    character.abortCast()

            member.sendPacket(SystemMessageId.PARTY_DISPERSED)
        }
        _members.clear()

        if (_positionBroadcastTask != null) {
            _positionBroadcastTask!!.cancel(false)
            _positionBroadcastTask = null
        }
    }

    /**
     * Get a random member from this party.
     * @param itemId : the ID of the item for which the member must have inventory space.
     * @param target : the object of which the member must be within a certain range (must not be null).
     * @return a random member from this party or `null` if none of the members have inventory space for the specified item.
     */
    private fun getRandomMember(itemId: Int, target: Creature): Player? {
        val availableMembers = ArrayList<Player>()
        for (member in _members) {
            if (member.inventory!!.validateCapacityByItemId(itemId) && MathUtil.checkIfInRange(
                    Config.PARTY_RANGE,
                    target,
                    member,
                    true
                )
            )
                availableMembers.add(member)
        }
        return if (availableMembers.isEmpty()) null else Rnd[availableMembers]
    }

    /**
     * Get the next item looter for this party.
     * @param itemId : the ID of the item for which the member must have inventory space.
     * @param target : the object of which the member must be within a certain range (must not be null).
     * @return the next looter from this party or `null` if none of the members have inventory space for the specified item.
     */
    private fun getNextLooter(itemId: Int, target: Creature): Player? {
        for (i in 0 until membersCount) {
            if (++_itemLastLoot >= membersCount)
                _itemLastLoot = 0

            val member = _members[_itemLastLoot]
            if (member.inventory!!.validateCapacityByItemId(itemId) && MathUtil.checkIfInRange(
                    Config.PARTY_RANGE,
                    target,
                    member,
                    true
                )
            )
                return member
        }
        return null
    }

    /**
     * @param player : the potential, initial looter.
     * @param itemId : the ID of the item for which the member must have inventory space.
     * @param spoil : a boolean used for spoil process.
     * @param target : the object of which the member must be within a certain range (must not be null).
     * @return the next Player looter.
     */
    private fun getActualLooter(player: Player, itemId: Int, spoil: Boolean, target: Creature): Player? {
        var looter: Player? = player

        when (lootRule) {
            Party.LootRule.ITEM_RANDOM -> if (!spoil)
                looter = getRandomMember(itemId, target)

            Party.LootRule.ITEM_RANDOM_SPOIL -> looter = getRandomMember(itemId, target)

            Party.LootRule.ITEM_ORDER -> if (!spoil)
                looter = getNextLooter(itemId, target)

            Party.LootRule.ITEM_ORDER_SPOIL -> looter = getNextLooter(itemId, target)
        }

        return looter ?: player
    }

    fun broadcastNewLeaderStatus() {
        val sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BECOME_A_PARTY_LEADER).addCharName(leader!!)
        for (member in _members) {
            member.sendPacket(PartySmallWindowDeleteAll.STATIC_PACKET)
            member.sendPacket(PartySmallWindowAll(member, this))
            member.broadcastUserInfo()
            member.sendPacket(sm)
        }
    }

    /**
     * Send a packet to all other players of the Party, except the player.
     * @param player : this player won't receive the packet.
     * @param msg : the packet to send.
     */
    fun broadcastToPartyMembers(player: Player?, msg: L2GameServerPacket) {
        for (member in _members) {
            if (member != null && member != player)
                member.sendPacket(msg)
        }
    }

    /**
     * Add a new member to the party.
     * @param player : the player to add to the party.
     */
    fun addPartyMember(player: Player?) {
        if (player == null || _members.contains(player))
            return

        // Send new member party window for all members.
        player.sendPacket(PartySmallWindowAll(player, this))
        broadcastPacket(PartySmallWindowAdd(player, this))

        // Send messages.
        player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_JOINED_S1_PARTY).addCharName(leader!!))
        broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_JOINED_PARTY).addCharName(player))

        // Cancel current rift session.
        DimensionalRiftManager.onPartyEdit(this)

        // Cancel party duel based on leader, as it will affect all players anyway.
        DuelManager.onPartyEdit(leader)

        // Add player to party.
        _members.add(player)

        // Add party to player.
        player.party = this

        // Adjust party level.
        if (player.level > level)
            level = player.level

        // Update icons.
        for (member in _members) {
            member.updateEffectIcons(true)
            member.broadcastUserInfo()
        }

        if (commandChannel != null)
            player.sendPacket(ExOpenMPCC.STATIC_PACKET)
    }

    /**
     * Removes a party member using its name.
     * @param name : player the player to remove from the party.
     * @param type : the message type [MessageType].
     */
    fun removePartyMember(name: String, type: MessageType) {
        removePartyMember(getPlayerByName(name), type)
    }

    /**
     * Removes a party member instance.
     * @param player : the player to remove from the party.
     * @param type : the message type [MessageType].
     */
    fun removePartyMember(player: Player?, type: MessageType) {
        if (player == null || !_members.contains(player))
            return

        if (_members.size == 2 || isLeader(player))
            disband()
        else {
            // Cancel current rift session.
            DimensionalRiftManager.onPartyEdit(this)

            // Cancel party duel based on leader, as it will affect all players anyway.
            DuelManager.onPartyEdit(leader)

            _members.remove(player)
            recalculateLevel()

            if (player.isFestivalParticipant)
                SevenSignsFestival.updateParticipants(player, this)

            if (player.fusionSkill != null)
                player.abortCast()

            for (character in player.getKnownType(Creature::class.java))
                if (character.fusionSkill != null && character.fusionSkill!!.target === player)
                    character.abortCast()

            if (type == MessageType.EXPELLED) {
                player.sendPacket(SystemMessageId.HAVE_BEEN_EXPELLED_FROM_PARTY)
                broadcastPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_WAS_EXPELLED_FROM_PARTY).addCharName(
                        player
                    )
                )
            } else if (type == MessageType.LEFT || type == MessageType.DISCONNECTED) {
                player.sendPacket(SystemMessageId.YOU_LEFT_PARTY)
                broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_LEFT_PARTY).addCharName(player))
            }

            player.party = null
            player.sendPacket(PartySmallWindowDeleteAll.STATIC_PACKET)

            broadcastPacket(PartySmallWindowDelete(player))

            if (commandChannel != null)
                player.sendPacket(ExCloseMPCC.STATIC_PACKET)
        }
    }

    /**
     * Change the party leader. If CommandChannel leader was the previous leader, change it too.
     * @param name : the name of the player newly promoted to leader.
     */
    fun changePartyLeader(name: String) {
        val player = getPlayerByName(name)
        if (player == null || player.isInDuel)
            return

        // Can't set leader if not part of the party.
        if (!_members.contains(player)) {
            player.sendPacket(SystemMessageId.YOU_CAN_TRANSFER_RIGHTS_ONLY_TO_ANOTHER_PARTY_MEMBER)
            return
        }

        // If already leader, abort.
        if (isLeader(player)) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_TRANSFER_RIGHTS_TO_YOURSELF)
            return
        }

        // Refresh channel leader, if any.
        if (commandChannel != null && commandChannel!!.isLeader(leader!!)) {
            commandChannel!!.leader = player
            commandChannel!!.broadcastPacket(
                SystemMessage.getSystemMessage(SystemMessageId.COMMAND_CHANNEL_LEADER_NOW_S1).addCharName(
                    player
                )
            )
        }

        // Update this party leader and broadcast the update.
        leader = player
        broadcastNewLeaderStatus()

        if (player.isInPartyMatchRoom) {
            val room = PartyMatchRoomList.getPlayerRoom(player)
            room!!.changeLeader(player)
        }
    }

    /**
     * @param name : the name of the player to search.
     * @return a party member by its name.
     */
    private fun getPlayerByName(name: String): Player? {
        for (member in _members) {
            if (member.name.equals(name, ignoreCase = true))
                return member
        }
        return null
    }

    /**
     * Distribute item(s) to one party member, based on party LootRule.
     * @param player : the initial looter.
     * @param item : the looted item to distribute.
     */
    fun distributeItem(player: Player, item: ItemInstance) {
        if (item.itemId == 57) {
            distributeAdena(player, item.count, player)
            item.destroyMe("Party", player, null)
            return
        }

        val target = getActualLooter(player, item.itemId, false, player) ?: return

        // Send messages to other party members about reward.
        if (item.count > 1)
            broadcastToPartyMembers(
                target,
                SystemMessage.getSystemMessage(SystemMessageId.S1_OBTAINED_S3_S2).addCharName(target).addItemName(item).addItemNumber(
                    item.count
                )
            )
        else if (item.enchantLevel > 0)
            broadcastToPartyMembers(
                target,
                SystemMessage.getSystemMessage(SystemMessageId.S1_OBTAINED_S2_S3).addCharName(target).addNumber(item.enchantLevel).addItemName(
                    item
                )
            )
        else
            broadcastToPartyMembers(
                target,
                SystemMessage.getSystemMessage(SystemMessageId.S1_OBTAINED_S2).addCharName(target).addItemName(item)
            )

        target.addItem("Party", item, player, true)
    }

    /**
     * Distribute item(s) to one party member, based on party LootRule.
     * @param player : the initial looter.
     * @param item : the looted item to distribute.
     * @param spoil : true if the item comes from a spoil process.
     * @param target : the looted character.
     */
    fun distributeItem(player: Player, item: IntIntHolder?, spoil: Boolean, target: Attackable) {
        if (item == null)
            return

        if (item.id == 57) {
            distributeAdena(player, item.value, target)
            return
        }

        val looter = getActualLooter(player, item.id, spoil, target) ?: return

        looter.addItem(if (spoil) "Sweep" else "Party", item.id, item.value, player, true)

        // Send messages to other party members about reward.
        val msg: SystemMessage
        if (item.value > 1) {
            msg =
                    if (spoil) SystemMessage.getSystemMessage(SystemMessageId.S1_SWEEPED_UP_S3_S2) else SystemMessage.getSystemMessage(
                        SystemMessageId.S1_OBTAINED_S3_S2
                    )
            msg.addCharName(looter)
            msg.addItemName(item.id)
            msg.addItemNumber(item.value)
        } else {
            msg =
                    if (spoil) SystemMessage.getSystemMessage(SystemMessageId.S1_SWEEPED_UP_S2) else SystemMessage.getSystemMessage(
                        SystemMessageId.S1_OBTAINED_S2
                    )
            msg.addCharName(looter)
            msg.addItemName(item.id)
        }
        broadcastToPartyMembers(looter, msg)
    }

    /**
     * Distribute adena to party members, according distance.
     * @param player : The player who picked.
     * @param adena : Amount of adenas.
     * @param target : Target used for distance checks.
     */
    fun distributeAdena(player: Player, adena: Int, target: Creature) {
        val toReward = ArrayList<Player>(_members.size)
        for (member in _members) {
            if (!MathUtil.checkIfInRange(Config.PARTY_RANGE, target, member, true) || member.adena == Integer.MAX_VALUE)
                continue

            toReward.add(member)
        }

        // Avoid divisions by 0.
        if (toReward.isEmpty())
            return

        val count = adena / toReward.size
        for (member in toReward)
            member.addAdena("Party", count, player, true)
    }

    /**
     * Distribute Experience and SP rewards to party members in the known area of the last attacker.<BR></BR>
     * <BR></BR>
     * <B><U> Actions</U> :</B><BR></BR>
     *
     *  * Get the owner of the Summon (if necessary).
     *  * Calculate the Experience and SP reward distribution rate.
     *  * Add Experience and SP to the player.
     *
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T GIVE rewards to Pet</B></FONT><BR></BR>
     * <BR></BR>
     * Exception are Pets that leech from the owner's XP; they get the exp indirectly, via the owner's exp gain.<BR></BR>
     * @param xpReward : The Experience reward to distribute.
     * @param spReward : The SP reward to distribute.
     * @param rewardedMembers : The list of Player to reward.
     * @param topLvl : The maximum level.
     * @param rewards : The list of players and summons.
     */
    fun distributeXpAndSp(
        xpReward: Long,
        spReward: Int,
        rewardedMembers: List<Player>,
        topLvl: Int,
        rewards: Map<Creature, RewardInfo>
    ) {
        var xpReward = xpReward
        var spReward = spReward
        val validMembers = ArrayList<Player>()

        if (Config.PARTY_XP_CUTOFF_METHOD.equals("level", ignoreCase = true)) {
            for (member in rewardedMembers) {
                if (topLvl - member.level <= Config.PARTY_XP_CUTOFF_LEVEL)
                    validMembers.add(member)
            }
        } else if (Config.PARTY_XP_CUTOFF_METHOD.equals("percentage", ignoreCase = true)) {
            var sqLevelSum = 0
            for (member in rewardedMembers)
                sqLevelSum += member.level * member.level

            for (member in rewardedMembers) {
                val sqLevel = member.level * member.level
                if (sqLevel * 100 >= sqLevelSum * Config.PARTY_XP_CUTOFF_PERCENT)
                    validMembers.add(member)
            }
        } else if (Config.PARTY_XP_CUTOFF_METHOD.equals("auto", ignoreCase = true)) {
            var sqLevelSum = 0
            for (member in rewardedMembers)
                sqLevelSum += member.level * member.level

            // Have to use range 1 to 9, since we -1 it : 0 can't be a good number (would lead to a IOOBE). Since 0 and 1 got same values, it's not a problem.
            val partySize = MathUtil.limit(rewardedMembers.size, 1, 9)

            for (member in rewardedMembers) {
                val sqLevel = member.level * member.level
                if (sqLevel >= sqLevelSum * (1 - 1 / (1 + BONUS_EXP_SP[partySize] - BONUS_EXP_SP[partySize - 1])))
                    validMembers.add(member)
            }
        }

        // Since validMembers can also hold CommandChannel members, we have to restrict the value.
        val partyRate = BONUS_EXP_SP[Math.min(validMembers.size, 9)]

        xpReward *= (partyRate * Config.RATE_PARTY_XP).toLong()
        spReward *= (partyRate * Config.RATE_PARTY_SP).toInt()

        var sqLevelSum = 0
        for (member in validMembers)
            sqLevelSum += member.level * member.level

        // Go through the players that must be rewarded.
        for (member in rewardedMembers) {
            if (member.isDead())
                continue

            // Calculate and add the EXP and SP reward to the member.
            if (validMembers.contains(member)) {
                // The servitor penalty.
                val penalty = if (member.hasServitor()) (member.pet as Servitor).expPenalty else 0f

                val sqLevel = (member.level * member.level).toDouble()
                val preCalculation = sqLevel / sqLevelSum * (1 - penalty)

                val xp = Math.round(xpReward * preCalculation)
                val sp = (spReward * preCalculation).toInt()

                // Set new karma.
                member.updateKarmaLoss(xp)

                // Add the XP/SP points to the requested party member.
                member.addExpAndSp(xp, sp, rewards)
            } else
                member.addExpAndSp(0, 0)
        }
    }

    /**
     * @return true if the entire party is currently dead.
     */
    fun wipedOut(): Boolean {
        for (member in _members) {
            if (!member.isDead())
                return false
        }
        return true
    }

    protected inner class PositionBroadcast : Runnable {
        override fun run() {
            if (_positionPacket == null)
                _positionPacket = PartyMemberPosition(this@Party)
            else
                _positionPacket!!.reuse(this@Party)

            broadcastPacket(_positionPacket!!)
        }
    }

    companion object {

        private val BONUS_EXP_SP = doubleArrayOf(1.0, 1.0, 1.30, 1.39, 1.50, 1.54, 1.58, 1.63, 1.67, 1.71)

        private val PARTY_POSITION_BROADCAST = 12000
    }
}