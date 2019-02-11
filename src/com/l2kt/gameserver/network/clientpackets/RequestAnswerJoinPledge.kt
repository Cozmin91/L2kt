package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.*

class RequestAnswerJoinPledge : L2GameClientPacket() {
    private var _answer: Int = 0

    override fun readImpl() {
        _answer = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val requestor = activeChar.request.partner ?: return

        if (_answer == 0) {
            activeChar.sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.YOU_DID_NOT_RESPOND_TO_S1_CLAN_INVITATION).addCharName(
                    requestor
                )
            )
            requestor.sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.S1_DID_NOT_RESPOND_TO_CLAN_INVITATION).addCharName(
                    activeChar
                )
            )
        } else {
            if (requestor.request.requestPacket !is RequestJoinPledge)
                return  // hax

            val requestPacket = requestor.request.requestPacket as RequestJoinPledge
            val clan = requestor.clan

            // we must double check this cause during response time conditions can be changed, i.e. another player could join clan
            if (clan.checkClanJoinCondition(requestor, activeChar, requestPacket.pledgeType)) {
                activeChar.sendPacket(JoinPledge(requestor.clanId))

                activeChar.pledgeType = requestPacket.pledgeType

                when (requestPacket.pledgeType) {
                    Clan.SUBUNIT_ACADEMY -> {
                        activeChar.powerGrade = 9
                        activeChar.lvlJoinedAcademy = activeChar.level
                    }

                    Clan.SUBUNIT_ROYAL1, Clan.SUBUNIT_ROYAL2 -> activeChar.powerGrade = 7

                    Clan.SUBUNIT_KNIGHT1, Clan.SUBUNIT_KNIGHT2, Clan.SUBUNIT_KNIGHT3, Clan.SUBUNIT_KNIGHT4 -> activeChar.powerGrade =
                            8

                    else -> activeChar.powerGrade = 6
                }

                clan.addClanMember(activeChar)
                activeChar.clanPrivileges = clan.getPriviledgesByRank(activeChar.powerGrade)

                activeChar.sendPacket(SystemMessageId.ENTERED_THE_CLAN)

                clan.broadcastToOtherOnlineMembers(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_JOINED_CLAN).addCharName(
                        activeChar
                    ), activeChar
                )
                clan.broadcastToOtherOnlineMembers(PledgeShowMemberListAdd(activeChar), activeChar)
                clan.broadcastToOnlineMembers(PledgeShowInfoUpdate(clan))

                // this activates the clan tab on the new member
                activeChar.sendPacket(PledgeShowMemberListAll(clan, 0))
                for (sp in activeChar.clan.allSubPledges)
                    activeChar.sendPacket(PledgeShowMemberListAll(clan, sp.id))

                activeChar.clanJoinExpiryTime = 0
                activeChar.broadcastUserInfo()
            }
        }
        activeChar.request.onRequestResponse()
    }
}