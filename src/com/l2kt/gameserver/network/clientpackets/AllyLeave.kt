package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.network.SystemMessageId

class AllyLeave : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val player = client.activeChar ?: return

        val clan = player.clan
        if (clan == null) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER)
            return
        }

        if (!player.isClanLeader) {
            player.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_WITHDRAW_ALLY)
            return
        }

        if (clan.allyId == 0) {
            player.sendPacket(SystemMessageId.NO_CURRENT_ALLIANCES)
            return
        }

        if (clan.clanId == clan.allyId) {
            player.sendPacket(SystemMessageId.ALLIANCE_LEADER_CANT_WITHDRAW)
            return
        }

        val currentTime = System.currentTimeMillis()
        clan.allyId = 0
        clan.allyName = null
        clan.changeAllyCrest(0, true)
        clan.setAllyPenaltyExpiryTime(
            currentTime + Config.ALT_ALLY_JOIN_DAYS_WHEN_LEAVED * 86400000L,
            Clan.PENALTY_TYPE_CLAN_LEAVED
        ) // 24*60*60*1000 = 86400000
        clan.updateClanInDB()

        player.sendPacket(SystemMessageId.YOU_HAVE_WITHDRAWN_FROM_ALLIANCE)
    }
}