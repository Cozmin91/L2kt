package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestSurrenderPersonally : L2GameClientPacket() {
    private var _pledgeName: String = ""

    override fun readImpl() {
        _pledgeName = readS()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val playerClan = activeChar.clan ?: return

        val clan = ClanTable.getClanByName(_pledgeName) ?: return

        if (!playerClan.isAtWarWith(clan.clanId) || activeChar.wantsPeace()) {
            activeChar.sendPacket(SystemMessageId.FAILED_TO_PERSONALLY_SURRENDER)
            return
        }

        activeChar.setWantsPeace(true)
        activeChar.deathPenalty(false, false, false)
        activeChar.sendPacket(
            SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_PERSONALLY_SURRENDERED_TO_THE_S1_CLAN).addString(
                _pledgeName
            )
        )
        ClanTable.checkSurrender(playerClan, clan)
    }
}