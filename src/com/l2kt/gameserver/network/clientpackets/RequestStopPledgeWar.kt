package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.network.SystemMessageId

class RequestStopPledgeWar : L2GameClientPacket() {
    private var _pledgeName: String = ""

    override fun readImpl() {
        _pledgeName = readS()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        val playerClan = player.clan ?: return

        val clan = ClanTable.getInstance().getClanByName(_pledgeName) ?: return

        if (player.clanPrivileges and Clan.CP_CL_PLEDGE_WAR != Clan.CP_CL_PLEDGE_WAR) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
            return
        }

        if (!playerClan.isAtWarWith(clan.clanId)) {
            player.sendPacket(SystemMessageId.NOT_INVOLVED_IN_WAR)
            return
        }

        for (member in playerClan.onlineMembers) {
            if (member.isInCombat) {
                player.sendPacket(SystemMessageId.CANT_STOP_CLAN_WAR_WHILE_IN_COMBAT)
                return
            }
        }

        ClanTable.getInstance().deleteClansWars(playerClan.clanId, clan.clanId)

        for (member in clan.onlineMembers)
            member.broadcastUserInfo()

        for (member in playerClan.onlineMembers)
            member.broadcastUserInfo()
    }
}