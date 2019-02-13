package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.network.serverpackets.PledgeReceiveMemberInfo

/**
 * Format: (ch) dSdS
 * @author -Wooden-
 */
class RequestPledgeReorganizeMember : L2GameClientPacket() {
    private var _isMemberSelected: Int = 0
    private var _memberName: String = ""
    private var _newPledgeType: Int = 0
    private var _selectedMember: String = ""

    override fun readImpl() {
        _isMemberSelected = readD()
        _memberName = readS()
        _newPledgeType = readD()
        _selectedMember = readS()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val clan = activeChar.clan ?: return

        if (activeChar.clanPrivileges and Clan.CP_CL_MANAGE_RANKS != Clan.CP_CL_MANAGE_RANKS)
            return

        val member1 = clan.getClanMember(_memberName)

        if (_isMemberSelected == 0) {
            if (member1 != null)
                activeChar.sendPacket(PledgeReceiveMemberInfo(member1)) // client changes affiliation info even if it fails, so we have to fix it manually
            return
        }

        val member2 = clan.getClanMember(_selectedMember)

        if (member1 == null || member1.objectId == clan.leaderId || member2 == null || member2.objectId == clan.leaderId)
            return

        // Do not send sub pledge leaders to other pledges than main
        if (clan.isSubPledgeLeader(member1.objectId)) {
            activeChar.sendPacket(PledgeReceiveMemberInfo(member1))
            return
        }

        val oldPledgeType = member1.pledgeType
        if (oldPledgeType == _newPledgeType)
            return

        member1.pledgeType = _newPledgeType
        member2.pledgeType = oldPledgeType

        clan.broadcastClanStatus()
    }
}