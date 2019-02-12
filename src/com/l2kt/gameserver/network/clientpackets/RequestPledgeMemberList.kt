package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.serverpackets.PledgeShowMemberListAll

class RequestPledgeMemberList : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val clan = activeChar.clan ?: return

        activeChar.sendPacket(PledgeShowMemberListAll(clan, 0))

        for (sp in clan.allSubPledges)
            activeChar.sendPacket(PledgeShowMemberListAll(clan, sp.id))
    }
}