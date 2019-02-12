package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.network.SystemMessageId

class AllyDismiss : L2GameClientPacket() {
    private var _clanName: String = ""

    override fun readImpl() {
        _clanName = readS()
    }

    override fun runImpl() {
        if (_clanName.isEmpty())
            return

        val player = client.activeChar ?: return

        val leaderClan = player.clan
        if (leaderClan == null) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER)
            return
        }

        if (leaderClan.allyId == 0) {
            player.sendPacket(SystemMessageId.NO_CURRENT_ALLIANCES)
            return
        }

        if (!player.isClanLeader || leaderClan.clanId != leaderClan.allyId) {
            player.sendPacket(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER)
            return
        }

        val clan = ClanTable.getInstance().getClanByName(_clanName)
        if (clan == null) {
            player.sendPacket(SystemMessageId.CLAN_DOESNT_EXISTS)
            return
        }

        if (clan.clanId == leaderClan.clanId) {
            player.sendPacket(SystemMessageId.ALLIANCE_LEADER_CANT_WITHDRAW)
            return
        }

        if (clan.allyId != leaderClan.allyId) {
            player.sendPacket(SystemMessageId.DIFFERENT_ALLIANCE)
            return
        }

        val currentTime = System.currentTimeMillis()
        leaderClan.setAllyPenaltyExpiryTime(
            currentTime + Config.ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED * 86400000L,
            Clan.PENALTY_TYPE_DISMISS_CLAN
        )
        leaderClan.updateClanInDB()

        clan.allyId = 0
        clan.allyName = null
        clan.changeAllyCrest(0, true)
        clan.setAllyPenaltyExpiryTime(
            currentTime + Config.ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED * 86400000L,
            Clan.PENALTY_TYPE_CLAN_DISMISSED
        )
        clan.updateClanInDB()

        player.sendPacket(SystemMessageId.YOU_HAVE_EXPELED_A_CLAN)
    }
}