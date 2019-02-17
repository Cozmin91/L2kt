package com.l2kt.gameserver.model.group

import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.CreatureSay
import com.l2kt.gameserver.network.serverpackets.ExCloseMPCC
import com.l2kt.gameserver.network.serverpackets.ExOpenMPCC
import com.l2kt.gameserver.network.serverpackets.L2GameServerPacket
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class CommandChannel(requestor: Party, target: Party) : AbstractGroup(requestor.leader) {
    private val _parties = CopyOnWriteArrayList<Party>()

    /**
     * **BEWARE : create a temporary List. Uses containsPlayer whenever possible.**
     */
    override val members: List<Player>
        get() {
            val members = ArrayList<Player>()
            for (party in _parties)
                members.addAll(party.members)

            return members
        }

    override val membersCount: Int
        get() {
            var count = 0
            for (party in _parties)
                count += party.membersCount

            return count
        }

    /**
     * @return the list of parties registered in this command channel.
     */
    val parties: List<Party>
        get() = _parties

    init {

        _parties.add(requestor)
        _parties.add(target)

        requestor.commandChannel = this
        target.commandChannel = this

        recalculateLevel()

        for (member in requestor.members) {
            member.sendPacket(SystemMessageId.COMMAND_CHANNEL_FORMED)
            member.sendPacket(ExOpenMPCC.STATIC_PACKET)
        }

        for (member in target.members) {
            member.sendPacket(SystemMessageId.JOINED_COMMAND_CHANNEL)
            member.sendPacket(ExOpenMPCC.STATIC_PACKET)
        }
    }

    override fun equals(obj: Any?): Boolean {
        if (obj !is CommandChannel)
            return false

        return if (obj === this) true else isLeader(obj.leader!!)

    }

    override fun containsPlayer(player: WorldObject): Boolean {
        for (party in _parties) {
            if (party.containsPlayer(player))
                return true
        }
        return false
    }

    override fun broadcastPacket(packet: L2GameServerPacket) {
        for (party in _parties)
            party.broadcastPacket(packet)
    }

    override fun broadcastCreatureSay(msg: CreatureSay, broadcaster: Player) {
        for (party in _parties)
            party.broadcastCreatureSay(msg, broadcaster)
    }

    override fun recalculateLevel() {
        var newLevel = 0
        for (party in _parties) {
            if (party.level > newLevel)
                newLevel = party.level
        }
        level = newLevel
    }

    override fun disband() {
        for (party in _parties) {
            party.commandChannel = null
            party.broadcastPacket(ExCloseMPCC.STATIC_PACKET)
            party.broadcastMessage(SystemMessageId.COMMAND_CHANNEL_DISBANDED)
        }
        _parties.clear()
    }

    /**
     * Adds a Party to the Command Channel.
     * @param party : the party to add.
     */
    fun addParty(party: Party?) {
        // Null party or party is already registered in this command channel.
        if (party == null || _parties.contains(party))
            return

        _parties.add(party)

        if (party.level > level)
            level = party.level

        party.commandChannel = this

        for (member in party.members) {
            member.sendPacket(SystemMessageId.JOINED_COMMAND_CHANNEL)
            member.sendPacket(ExOpenMPCC.STATIC_PACKET)
        }
    }

    /**
     * Removes a Party from the Command Channel.
     * @param party : the party to remove. Disband the channel if there was only 2 parties left.
     * @return true if the party has been successfully removed from command channel.
     */
    fun removeParty(party: Party?): Boolean {
        // Null party or party isn't registered in this command channel.
        if (party == null || !_parties.contains(party))
            return false

        // Don't bother individually drop parties, disband entirely if there is only 2 parties in command channel.
        if (_parties.size == 2)
            disband()
        else {
            _parties.remove(party)

            party.commandChannel = null
            party.broadcastPacket(ExCloseMPCC.STATIC_PACKET)

            recalculateLevel()
        }
        return true
    }

    /**
     * @param attackable : the attackable to check.
     * @return true if the members count is reached.
     */
    fun meetRaidWarCondition(attackable: Attackable): Boolean {
        when (attackable.npcId) {
            29001 // Queen Ant
                , 29006 // Core
                , 29014 // Orfen
                , 29022 // Zaken
            -> return membersCount > 36

            29020 // Baium
            -> return membersCount > 56

            29019 // Antharas
            -> return membersCount > 225

            29028 // Valakas
            -> return membersCount > 99

            else // normal Raidboss
            -> return membersCount > 18
        }
    }
}