package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.sql.ClanTable

class RequestReplySurrenderPledgeWar : L2GameClientPacket() {
    private var _answer: Int = 0

    override fun readImpl() {
        _answer = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val requestor = activeChar.activeRequester ?: return

        if (_answer == 1) {
            requestor.deathPenalty(false, false, false)
            ClanTable.getInstance().deleteClansWars(requestor.clanId, activeChar.clanId)
        }

        activeChar.onTransactionRequest(requestor)
    }
}