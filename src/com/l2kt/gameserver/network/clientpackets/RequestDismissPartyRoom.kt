package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.partymatching.PartyMatchRoomList

class RequestDismissPartyRoom : L2GameClientPacket() {
    private var _roomid: Int = 0
    private var _data2: Int = 0

    override fun readImpl() {
        _roomid = readD()
        _data2 = readD()
    }

    override fun runImpl() {
        client.activeChar ?: return

        PartyMatchRoomList.getRoom(_roomid) ?: return

        PartyMatchRoomList.deleteRoom(_roomid)
    }
}