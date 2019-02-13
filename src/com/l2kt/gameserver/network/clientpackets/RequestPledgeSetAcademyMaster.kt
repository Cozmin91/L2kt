package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.model.pledge.ClanMember
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.PledgeShowMemberListUpdate
import com.l2kt.gameserver.network.serverpackets.SystemMessage

/**
 * Format: (ch) dSS
 * @author -Wooden-
 */
class RequestPledgeSetAcademyMaster : L2GameClientPacket() {
    private var _currPlayerName: String = ""
    private var _set: Int = 0 // 1 set, 0 delete
    private var _targetPlayerName: String = ""

    override fun readImpl() {
        _set = readD()
        _currPlayerName = readS()
        _targetPlayerName = readS()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val clan = activeChar.clan ?: return

        if (activeChar.clanPrivileges and Clan.CP_CL_MASTER_RIGHTS != Clan.CP_CL_MASTER_RIGHTS) {
            activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_DISMISS_AN_APPRENTICE)
            return
        }

        val currentMember = clan.getClanMember(_currPlayerName)
        val targetMember = clan.getClanMember(_targetPlayerName)
        if (currentMember == null || targetMember == null)
            return

        val apprenticeMember: ClanMember
        val sponsorMember: ClanMember
        if (currentMember.pledgeType == Clan.SUBUNIT_ACADEMY) {
            apprenticeMember = currentMember
            sponsorMember = targetMember
        } else {
            apprenticeMember = targetMember
            sponsorMember = currentMember
        }

        val apprentice = apprenticeMember.playerInstance
        val sponsor = sponsorMember.playerInstance

        val sm: SystemMessage?
        if (_set == 0) {
            // test: do we get the current sponsor & apprentice from this packet or no?
            if (apprentice != null)
                apprentice.sponsor = 0
            else
            // offline
                apprenticeMember.setApprenticeAndSponsor(0, 0)

            if (sponsor != null)
                sponsor.apprentice = 0
            else
            // offline
                sponsorMember.setApprenticeAndSponsor(0, 0)

            apprenticeMember.saveApprenticeAndSponsor(0, 0)
            sponsorMember.saveApprenticeAndSponsor(0, 0)

            sm = SystemMessage.getSystemMessage(SystemMessageId.S2_CLAN_MEMBER_S1_APPRENTICE_HAS_BEEN_REMOVED)
        } else {
            if (apprenticeMember.sponsor != 0 || sponsorMember.apprentice != 0 || apprenticeMember.apprentice != 0 || sponsorMember.sponsor != 0) {
                activeChar.sendMessage("Remove previous connections first.")
                return
            }

            if (apprentice != null)
                apprentice.sponsor = sponsorMember.objectId
            else
            // offline
                apprenticeMember.setApprenticeAndSponsor(0, sponsorMember.objectId)

            if (sponsor != null)
                sponsor.apprentice = apprenticeMember.objectId
            else
            // offline
                sponsorMember.setApprenticeAndSponsor(apprenticeMember.objectId, 0)

            // saving to database even if online, since both must match
            apprenticeMember.saveApprenticeAndSponsor(0, sponsorMember.objectId)
            sponsorMember.saveApprenticeAndSponsor(apprenticeMember.objectId, 0)

            sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HAS_BEEN_DESIGNATED_AS_APPRENTICE_OF_CLAN_MEMBER_S1)
        }
        sm.addString(sponsorMember.name!!)
        sm.addString(apprenticeMember.name!!)

        if (sponsor != activeChar && sponsor != apprentice)
            activeChar.sendPacket(sm)

        sponsor?.sendPacket(sm)

        apprentice?.sendPacket(sm)

        clan.broadcastToOnlineMembers(
            PledgeShowMemberListUpdate(sponsorMember),
            PledgeShowMemberListUpdate(apprenticeMember)
        )
    }
}