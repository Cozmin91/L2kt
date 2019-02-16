package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.partymatching.PartyMatchRoomList
import com.l2kt.gameserver.model.partymatching.PartyMatchWaitingList

class ExListPartyMatchingWaitingRoom(
    private val _activeChar: Player,
    private val _page: Int,
    private val _minlvl: Int,
    private val _maxlvl: Int,
    private val _mode: Int
) : L2GameServerPacket() {
    private val _members = mutableListOf<Player>()

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x35)

        // If the mode is 0 and the activeChar isn't the PartyRoom leader, return an empty list.
        if (_mode == 0) {
            // Retrieve the activeChar PartyMatchRoom
            val room = PartyMatchRoomList.getInstance().getRoom(_activeChar.partyRoom)
            if (room == null || room.owner != _activeChar) {
                writeD(0)
                writeD(0)
                return
            }
        }

        for (cha in PartyMatchWaitingList.players) {
            // Don't add yourself in the list
            if (cha == null || cha == _activeChar)
                continue

            if (cha.level < _minlvl || cha.level > _maxlvl)
                continue

            _members.add(cha)
        }

        writeD(1)
        writeD(_members.size)
        for (member in _members) {
            writeS(member.name)
            writeD(member.activeClass)
            writeD(member.level)
        }
    }
}