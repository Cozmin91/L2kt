package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.partymatching.PartyMatchRoomList
import com.l2kt.gameserver.model.partymatching.PartyMatchWaitingList
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.ExPartyRoomMember
import com.l2kt.gameserver.network.serverpackets.PartyMatchDetail
import com.l2kt.gameserver.network.serverpackets.PartyMatchList

class RequestPartyMatchConfig : L2GameClientPacket() {
    private var _auto: Int = 0
    private var _loc: Int = 0
    private var _lvl: Int = 0

    override fun readImpl() {
        _auto = readD()
        _loc = readD()
        _lvl = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        if (!activeChar.isInPartyMatchRoom && activeChar.party != null && activeChar.party!!.leader != activeChar) {
            activeChar.sendPacket(SystemMessageId.CANT_VIEW_PARTY_ROOMS)
            activeChar.sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        if (activeChar.isInPartyMatchRoom) {
            // If Player is in Room show him room, not list
            val list = PartyMatchRoomList ?: return

            val room = list.getPlayerRoom(activeChar) ?: return

            activeChar.sendPacket(PartyMatchDetail(room))
            activeChar.sendPacket(ExPartyRoomMember(room, 2))

            activeChar.partyRoom = room.id
            activeChar.broadcastUserInfo()
        } else {
            // Add to waiting list
            PartyMatchWaitingList.addPlayer(activeChar)

            // Send Room list
            activeChar.sendPacket(PartyMatchList(activeChar, _auto, _loc, _lvl))
        }
    }
}