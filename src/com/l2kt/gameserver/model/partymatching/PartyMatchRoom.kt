package com.l2kt.gameserver.model.partymatching

import com.l2kt.gameserver.data.xml.MapRegionData
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ExManagePartyRoomMember
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.util.*

class PartyMatchRoom(
    val id: Int,
    var title: String?,
    var lootType: Int,
    var minLvl: Int,
    var maxLvl: Int,
    var maxMembers: Int,
    owner: Player
) {
    var location: Int = 0
    private val _members = ArrayList<Player>()

    val partyMembers: List<Player>
        get() = _members

    val owner: Player
        get() = _members[0]

    val members: Int
        get() = _members.size

    init {
        location = MapRegionData.getClosestLocation(owner.x, owner.y)
        _members.add(owner)
    }

    fun addMember(player: Player) {
        _members.add(player)
    }

    fun deleteMember(player: Player) {
        if (player != owner) {
            _members.remove(player)
            notifyMembersAboutExit(player)
        } else if (_members.size == 1) {
            PartyMatchRoomList.deleteRoom(id)
        } else {
            changeLeader(_members[1])
            deleteMember(player)
        }
    }

    fun notifyMembersAboutExit(player: Player) {
        for (_member in partyMembers) {
            val sm = SystemMessage.getSystemMessage(SystemMessageId.S1_LEFT_PARTY_ROOM)
            sm.addCharName(player)
            _member.sendPacket(sm)
            _member.sendPacket(ExManagePartyRoomMember(player, this, 2))
        }
    }

    fun changeLeader(newLeader: Player) {
        // Get current leader
        val oldLeader = _members[0]
        // Remove new leader
        _members.remove(newLeader)
        // Move him to first position
        _members[0] = newLeader
        // Add old leader as normal member
        _members.add(oldLeader)
        // Broadcast change
        for (member in partyMembers) {
            member.sendPacket(ExManagePartyRoomMember(newLeader, this, 1))
            member.sendPacket(ExManagePartyRoomMember(oldLeader, this, 1))
            member.sendPacket(SystemMessageId.PARTY_ROOM_LEADER_CHANGED)
        }
    }
}