package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestSurrenderPledgeWar : L2GameClientPacket() {
    private var _pledgeName: String = ""

    override fun readImpl() {
        _pledgeName = readS()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val playerClan = activeChar.clan ?: return

        // Check if player who does the request has the correct rights to do it
        if (activeChar.clanPrivileges and Clan.CP_CL_PLEDGE_WAR != Clan.CP_CL_PLEDGE_WAR) {
            activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
            return
        }

        val clan = ClanTable.getClanByName(_pledgeName) ?: return

        if (!playerClan.isAtWarWith(clan.clanId)) {
            activeChar.sendPacket(SystemMessageId.NOT_INVOLVED_IN_WAR)
            return
        }

        activeChar.deathPenalty(false, false, false)
        activeChar.sendPacket(
            SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN).addString(
                _pledgeName
            )
        )
        ClanTable.deleteClansWars(playerClan.clanId, clan.clanId)
    }
}