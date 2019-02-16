package com.l2kt.gameserver.model.partymatching

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ExClosePartyRoom
import java.util.*

/**
 * @author Gnacik
 */
object PartyMatchRoomList {
    var maxId = 1
        private set
    private val _rooms: MutableMap<Int, PartyMatchRoom>

    val rooms: Array<PartyMatchRoom>
        get() = _rooms.values.toTypedArray()

    val partyMatchRoomCount: Int
        get() = _rooms.size

    init {
        _rooms = HashMap()
    }

    @Synchronized
    fun addPartyMatchRoom(id: Int, room: PartyMatchRoom) {
        _rooms[id] = room
        maxId++
    }

    fun deleteRoom(id: Int) {
        for (_member in getRoom(id)?.partyMembers ?: emptyList()) {
            _member.sendPacket(ExClosePartyRoom.STATIC_PACKET)
            _member.sendPacket(SystemMessageId.PARTY_ROOM_DISBANDED)

            _member.partyRoom = 0
            _member.broadcastUserInfo()
        }
        _rooms.remove(id)
    }

    fun getRoom(id: Int): PartyMatchRoom? {
        return _rooms[id]
    }

    fun getPlayerRoom(player: Player): PartyMatchRoom? {
        for (_room in _rooms.values)
            for (member in _room.partyMembers)
                if (member == player)
                    return _room

        return null
    }

    fun getPlayerRoomId(player: Player): Int {
        for (_room in _rooms.values)
            for (member in _room.partyMembers)
                if (member == player)
                    return _room.id

        return -1
    }
}