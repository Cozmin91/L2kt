package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.serverpackets.PledgePowerGradeList

class RequestPledgePowerGradeList : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val player = client.activeChar ?: return

        val clan = player.clan ?: return

        player.sendPacket(PledgePowerGradeList(clan.priviledges.keys, clan.members))
    }
}