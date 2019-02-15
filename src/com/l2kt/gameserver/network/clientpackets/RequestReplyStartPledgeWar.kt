package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.network.SystemMessageId

class RequestReplyStartPledgeWar : L2GameClientPacket() {
    private var _answer: Int = 0

    override fun readImpl() {
        _answer = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val requestor = activeChar.activeRequester ?: return

        if (_answer == 1)
            ClanTable.storeClansWars(requestor.clanId, activeChar.clanId)
        else
            requestor.sendPacket(SystemMessageId.WAR_PROCLAMATION_HAS_BEEN_REFUSED)

        activeChar.activeRequester = null
        requestor.onTransactionResponse()
    }
}