package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.partymatching.PartyMatchRoom
import com.l2kt.gameserver.model.partymatching.PartyMatchRoomList
import java.util.*

class PartyMatchList(private val _cha: Player, auto: Int, private val _loc: Int, private val _lim: Int) :
    L2GameServerPacket() {
    private val _rooms: MutableList<PartyMatchRoom>

    init {
        _rooms = ArrayList()
    }

    override fun writeImpl() {
        if (client.activeChar == null)
            return

        for (room in PartyMatchRoomList.getInstance().rooms) {
            if (room.members < 1 || room.owner == null || !room.owner.isOnline || room.owner.partyRoom != room.id) {
                PartyMatchRoomList.getInstance().deleteRoom(room.id)
                continue
            }

            if (_loc > 0 && _loc != room.location)
                continue

            if (_lim == 0 && (_cha.level < room.minLvl || _cha.level > room.maxLvl))
                continue

            _rooms.add(room)
        }

        writeC(0x96)
        writeD(if (!_rooms.isEmpty()) 1 else 0)
        writeD(_rooms.size)
        for (room in _rooms) {
            writeD(room.id)
            writeS(room.title)
            writeD(room.location)
            writeD(room.minLvl)
            writeD(room.maxLvl)
            writeD(room.members)
            writeD(room.maxMembers)
            writeS(room.owner.name)
        }
    }
}