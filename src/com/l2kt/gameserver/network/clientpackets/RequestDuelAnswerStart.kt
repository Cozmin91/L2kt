package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.manager.DuelManager
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestDuelAnswerStart : L2GameClientPacket() {
    private var _partyDuel: Int = 0
    private var _unk1: Int = 0
    private var _response: Int = 0

    override fun readImpl() {
        _partyDuel = readD()
        _unk1 = readD()
        _response = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val requestor = activeChar.activeRequester ?: return

        activeChar.activeRequester = null
        requestor.onTransactionResponse()

        if (_response == 1) {
            // Check if duel is possible.
            if (!requestor.canDuel()) {
                activeChar.sendPacket(requestor.noDuelReason)
                return
            }

            if (!activeChar.canDuel()) {
                activeChar.sendPacket(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME)
                return
            }

            // Players musn't be too far.
            if (!requestor.isInsideRadius(activeChar, 2000, false, false)) {
                activeChar.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_RECEIVE_A_DUEL_CHALLENGE_BECAUSE_S1_IS_TOO_FAR_AWAY).addCharName(
                        requestor
                    )
                )
                return
            }

            if (_partyDuel == 1) {
                // Player must be a party leader, the target can't be of the same party.
                val requestorParty = requestor.party
                if (requestorParty == null || !requestorParty.isLeader(requestor) || requestorParty.containsPlayer(
                        activeChar
                    )
                ) {
                    activeChar.sendPacket(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME)
                    return
                }

                // Target must be in a party.
                val activeCharParty = activeChar.party
                if (activeCharParty == null) {
                    activeChar.sendPacket(SystemMessageId.SINCE_THE_PERSON_YOU_CHALLENGED_IS_NOT_CURRENTLY_IN_A_PARTY_THEY_CANNOT_DUEL_AGAINST_YOUR_PARTY)
                    return
                }

                // Check if every player is ready for a duel.
                for (member in requestorParty.members) {
                    if (member != requestor && !member.canDuel()) {
                        activeChar.sendPacket(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME)
                        return
                    }
                }

                for (member in activeCharParty.members) {
                    if (member != activeChar && !member.canDuel()) {
                        activeChar.sendPacket(SystemMessageId.THE_OPPOSING_PARTY_IS_CURRENTLY_UNABLE_TO_ACCEPT_A_CHALLENGE_TO_A_DUEL)
                        return
                    }
                }

                // Drop command channels, for both requestor && player parties.
                val requestorChannel = requestorParty.commandChannel
                requestorChannel?.removeParty(requestorParty)

                val activeCharChannel = activeCharParty.commandChannel
                activeCharChannel?.removeParty(activeCharParty)

                // Partymatching
                for (member in requestorParty.members)
                    member.removeMeFromPartyMatch()

                for (member in activeCharParty.members)
                    member.removeMeFromPartyMatch()

                activeChar.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACCEPTED_S1_CHALLENGE_TO_A_PARTY_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS).addCharName(
                        requestor
                    )
                )
                requestor.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_DUEL_AGAINST_THEIR_PARTY_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS).addCharName(
                        activeChar
                    )
                )
            } else {
                // Partymatching
                activeChar.removeMeFromPartyMatch()
                requestor.removeMeFromPartyMatch()

                activeChar.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACCEPTED_S1_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS).addCharName(
                        requestor
                    )
                )
                requestor.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS).addCharName(
                        activeChar
                    )
                )
            }

            DuelManager.getInstance().addDuel(requestor, activeChar, _partyDuel == 1)
        } else {
            if (_partyDuel == 1)
                requestor.sendPacket(SystemMessageId.THE_OPPOSING_PARTY_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL)
            else
                requestor.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL).addCharName(
                        activeChar
                    )
                )
        }
    }
}