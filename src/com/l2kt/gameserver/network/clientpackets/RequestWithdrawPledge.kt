package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.PledgeShowMemberListDelete
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestWithdrawPledge : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val clan = activeChar.clan
        if (clan == null) {
            activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER)
            return
        }

        if (activeChar.isClanLeader) {
            activeChar.sendPacket(SystemMessageId.CLAN_LEADER_CANNOT_WITHDRAW)
            return
        }

        if (activeChar.isInCombat) {
            activeChar.sendPacket(SystemMessageId.YOU_CANNOT_LEAVE_DURING_COMBAT)
            return
        }

        clan.removeClanMember(activeChar.objectId, System.currentTimeMillis() + Config.ALT_CLAN_JOIN_DAYS * 86400000L)
        clan.broadcastToOnlineMembers(
            SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WITHDRAWN_FROM_THE_CLAN).addCharName(
                activeChar
            )
        )

        // Remove the player from the members list.
        if (clan.isSubPledgeLeader(activeChar.objectId))
            clan.broadcastClanStatus() // refresh list
        else
            clan.broadcastToOnlineMembers(PledgeShowMemberListDelete(activeChar.name))

        activeChar.sendPacket(SystemMessageId.YOU_HAVE_WITHDRAWN_FROM_CLAN)
        activeChar.sendPacket(SystemMessageId.YOU_MUST_WAIT_BEFORE_JOINING_ANOTHER_CLAN)
    }
}