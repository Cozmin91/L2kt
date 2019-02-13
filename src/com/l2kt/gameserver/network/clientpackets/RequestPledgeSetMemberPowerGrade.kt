package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.PledgeShowMemberListUpdate
import com.l2kt.gameserver.network.serverpackets.SystemMessage

/**
 * Format: (ch) Sd
 * @author -Wooden-
 */
class RequestPledgeSetMemberPowerGrade : L2GameClientPacket() {
    private var _powerGrade: Int = 0
    private var _member: String? = null

    override fun readImpl() {
        _member = readS()
        _powerGrade = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val clan = activeChar.clan ?: return

        val member = clan.getClanMember(_member) ?: return

        if (member.pledgeType == Clan.SUBUNIT_ACADEMY)
            return

        member.powerGrade = _powerGrade
        clan.broadcastToOnlineMembers(
            PledgeShowMemberListUpdate(member),
            SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_PRIVILEGE_CHANGED_TO_S2).addString(member.name!!).addNumber(
                _powerGrade
            )
        )
    }
}