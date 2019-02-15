package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.network.SystemMessageId

class RequestReplyStopPledgeWar : L2GameClientPacket() {
    private var _answer: Int = 0

    override fun readImpl() {
        _answer = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val requestor = activeChar.activeRequester ?: return

        if (_answer == 1)
            ClanTable.deleteClansWars(requestor.clanId, activeChar.clanId)
        else
            requestor.sendPacket(SystemMessageId.REQUEST_TO_END_WAR_HAS_BEEN_DENIED)

        activeChar.activeRequester = null
        requestor.onTransactionResponse()
    }
}