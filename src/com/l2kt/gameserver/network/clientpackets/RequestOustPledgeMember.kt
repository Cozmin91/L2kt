package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.PledgeShowMemberListDelete
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestOustPledgeMember : L2GameClientPacket() {
    private var _target: String = ""

    override fun readImpl() {
        _target = readS()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        val clan = player.clan
        if (clan == null) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER)
            return
        }

        val member = clan.getClanMember(_target) ?: return

        if (player.clanPrivileges and Clan.CP_CL_DISMISS != Clan.CP_CL_DISMISS) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
            return
        }

        if (player.name.equals(_target, ignoreCase = true)) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_DISMISS_YOURSELF)
            return
        }

        if (member.isOnline && member.playerInstance != null && member.playerInstance!!.isInCombat) {
            player.sendPacket(SystemMessageId.CLAN_MEMBER_CANNOT_BE_DISMISSED_DURING_COMBAT)
            return
        }

        // this also updates the database
        clan.removeClanMember(member.objectId, System.currentTimeMillis() + Config.ALT_CLAN_JOIN_DAYS * 86400000L)
        clan.charPenaltyExpiryTime = System.currentTimeMillis() + Config.ALT_CLAN_JOIN_DAYS * 86400000L
        clan.updateClanInDB()

        // Remove the player from the members list.
        if (clan.isSubPledgeLeader(member.objectId))
            clan.broadcastClanStatus() // refresh clan tab
        else
            clan.broadcastToOnlineMembers(PledgeShowMemberListDelete(_target))

        clan.broadcastToOnlineMembers(
            SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_EXPELLED).addString(
                member.name ?: ""
            )
        )
        player.sendPacket(SystemMessageId.YOU_HAVE_SUCCEEDED_IN_EXPELLING_CLAN_MEMBER)
        player.sendPacket(SystemMessageId.YOU_MUST_WAIT_BEFORE_ACCEPTING_A_NEW_MEMBER)

        if (member.isOnline)
            member.playerInstance?.sendPacket(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED)
    }
}