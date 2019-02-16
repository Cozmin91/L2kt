package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.partymatching.PartyMatchRoomList
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ExClosePartyRoom

class RequestWithdrawPartyRoom : L2GameClientPacket() {
    private var _roomid: Int = 0
    private var _unk1: Int = 0

    override fun readImpl() {
        _roomid = readD()
        _unk1 = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val room = PartyMatchRoomList.getRoom(_roomid) ?: return

        if (activeChar.isInParty && room.owner.isInParty && activeChar.party!!.leaderObjectId == room.owner.party!!.leaderObjectId) {
            // If user is in party with Room Owner is not removed from Room
        } else {
            room.deleteMember(activeChar)
            activeChar.partyRoom = 0
            activeChar.broadcastUserInfo()

            activeChar.sendPacket(ExClosePartyRoom.STATIC_PACKET)
            activeChar.sendPacket(SystemMessageId.PARTY_ROOM_EXITED)
        }
    }
}