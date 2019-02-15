package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestStartPledgeWar : L2GameClientPacket() {
    private var _pledgeName: String = ""

    override fun readImpl() {
        _pledgeName = readS()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        val attackerClan = player.clan ?: return

        if (player.clanPrivileges and Clan.CP_CL_PLEDGE_WAR != Clan.CP_CL_PLEDGE_WAR) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
            return
        }

        val attackedClan = ClanTable.getClanByName(_pledgeName)
        if (attackedClan == null) {
            player.sendPacket(SystemMessageId.CLAN_WAR_CANNOT_DECLARED_CLAN_NOT_EXIST)
            return
        }

        if (attackedClan.clanId == attackerClan.clanId) {
            player.sendPacket(SystemMessageId.CANNOT_DECLARE_AGAINST_OWN_CLAN)
            return
        }

        if (attackerClan.warList.size >= 30) {
            player.sendPacket(SystemMessageId.TOO_MANY_CLAN_WARS)
            return
        }

        if (attackerClan.level < 3 || attackerClan.membersCount < Config.ALT_CLAN_MEMBERS_FOR_WAR) {
            player.sendPacket(SystemMessageId.CLAN_WAR_DECLARED_IF_CLAN_LVL3_OR_15_MEMBER)
            return
        }

        if (!attackerClan.attackerList.contains(attackedClan.clanId) && (attackedClan.level < 3 || attackedClan.membersCount < Config.ALT_CLAN_MEMBERS_FOR_WAR)) {
            player.sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.S1_CLAN_CANNOT_DECLARE_WAR_TOO_LOW_LEVEL_OR_NOT_ENOUGH_MEMBERS).addString(
                    attackedClan.name
                )
            )
            return
        }

        if (attackerClan.allyId == attackedClan.allyId && attackerClan.allyId != 0) {
            player.sendPacket(SystemMessageId.CLAN_WAR_AGAINST_A_ALLIED_CLAN_NOT_WORK)
            return
        }

        if (attackedClan.dissolvingExpiryTime > 0) {
            player.sendPacket(SystemMessageId.NO_CLAN_WAR_AGAINST_DISSOLVING_CLAN)
            return
        }

        if (attackerClan.isAtWarWith(attackedClan.clanId)) {
            player.sendPacket(SystemMessageId.WAR_ALREADY_DECLARED)
            return
        }

        if (attackerClan.hasWarPenaltyWith(attackedClan.clanId)) {
            player.sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.ALREADY_AT_WAR_WITH_S1_WAIT_5_DAYS).addString(
                    attackedClan.name
                )
            )
            return
        }

        ClanTable.storeClansWars(player.clanId, attackedClan.clanId)

        for (member in attackedClan.onlineMembers)
            member.broadcastUserInfo()

        for (member in attackerClan.onlineMembers)
            member.broadcastUserInfo()
    }
}