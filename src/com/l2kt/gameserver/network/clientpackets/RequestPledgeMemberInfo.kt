package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.serverpackets.PledgeReceiveMemberInfo

/**
 * Format: (ch) dS
 * @author -Wooden-
 */
class RequestPledgeMemberInfo : L2GameClientPacket() {
    private var _pledgeType: Int = 0
    private var _player: String? = null

    override fun readImpl() {
        _pledgeType = readD()
        _player = readS()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val clan = activeChar.clan ?: return

        val member = clan.getClanMember(_player) ?: return

        activeChar.sendPacket(PledgeReceiveMemberInfo(member))
    }
}