package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.network.SystemMessageId

class RequestAnswerJoinAlly : L2GameClientPacket() {
    private var _response: Int = 0

    override fun readImpl() {
        _response = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val requestor = activeChar.request.partner ?: return

        if (_response == 0) {
            activeChar.sendPacket(SystemMessageId.YOU_DID_NOT_RESPOND_TO_ALLY_INVITATION)
            requestor.sendPacket(SystemMessageId.NO_RESPONSE_TO_ALLY_INVITATION)
        } else {
            if (requestor.request.requestPacket !is RequestJoinAlly)
                return

            if (!Clan.checkAllyJoinCondition(requestor, activeChar))
                return

            activeChar.clan.allyId = requestor.clan.allyId
            activeChar.clan.allyName = requestor.clan.allyName
            activeChar.clan.setAllyPenaltyExpiryTime(0, 0)
            activeChar.clan.changeAllyCrest(requestor.clan.allyCrestId, true)
            activeChar.clan.updateClanInDB()

            activeChar.sendPacket(SystemMessageId.YOU_ACCEPTED_ALLIANCE)
        }
        activeChar.request.onRequestResponse()
    }
}