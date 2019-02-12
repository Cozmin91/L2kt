package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.BlockList
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.group.Party
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.AskJoinParty
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestJoinParty : L2GameClientPacket() {
    private var _name: String = ""
    private var _itemDistribution: Int = 0

    override fun readImpl() {
        _name = readS()
        _itemDistribution = readD()
    }

    override fun runImpl() {
        val requestor = client.activeChar ?: return

        val target = World.getInstance().getPlayer(_name)
        if (target == null) {
            requestor.sendPacket(SystemMessageId.FIRST_SELECT_USER_TO_INVITE_TO_PARTY)
            return
        }

        if (BlockList.isBlocked(target, requestor)) {
            requestor.sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST).addCharName(
                    target
                )
            )
            return
        }

        if (target == requestor || target.isCursedWeaponEquipped || requestor.isCursedWeaponEquipped || target.appearance.invisible) {
            requestor.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET)
            return
        }

        if (target.isInParty) {
            requestor.sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.S1_IS_ALREADY_IN_PARTY).addCharName(
                    target
                )
            )
            return
        }

        if (target.client.isDetached) {
            requestor.sendMessage("The player you tried to invite is in offline mode.")
            return
        }

        if (target.isInJail || requestor.isInJail) {
            requestor.sendMessage("The player you tried to invite is currently jailed.")
            return
        }

        if (target.isInOlympiadMode || requestor.isInOlympiadMode)
            return

        if (requestor.isProcessingRequest) {
            requestor.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY)
            return
        }

        if (target.isProcessingRequest) {
            requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addCharName(target))
            return
        }

        val party = requestor.party
        if (party != null) {
            if (!party.isLeader(requestor)) {
                requestor.sendPacket(SystemMessageId.ONLY_LEADER_CAN_INVITE)
                return
            }

            if (party.membersCount >= 9) {
                requestor.sendPacket(SystemMessageId.PARTY_FULL)
                return
            }

            if (party.pendingInvitation && !party.isInvitationRequestExpired) {
                requestor.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY)
                return
            }

            party.pendingInvitation = true
        } else
            requestor.lootRule = Party.LootRule.VALUES[_itemDistribution]

        requestor.onTransactionRequest(target)
        requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_INVITED_S1_TO_PARTY).addCharName(target))

        target.sendPacket(AskJoinParty(requestor.name, party?.lootRule?.ordinal ?: _itemDistribution))
    }
}