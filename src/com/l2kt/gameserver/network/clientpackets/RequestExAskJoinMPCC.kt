package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ExAskJoinMPCC
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestExAskJoinMPCC : L2GameClientPacket() {
    private var _name: String = ""

    override fun readImpl() {
        _name = readS()
    }

    override fun runImpl() {
        val requestor = client.activeChar ?: return

        val target = World.getPlayer(_name) ?: return

        val requestorParty = requestor.party ?: return

        val targetParty = target.party
        if (targetParty == null || requestorParty == targetParty)
            return

        if (!requestorParty.isLeader(requestor)) {
            requestor.sendPacket(SystemMessageId.CANNOT_INVITE_TO_COMMAND_CHANNEL)
            return
        }

        val requestorChannel = requestorParty.commandChannel
        if (requestorChannel != null && !requestorChannel.isLeader(requestor)) {
            requestor.sendPacket(SystemMessageId.CANNOT_INVITE_TO_COMMAND_CHANNEL)
            return
        }

        val targetChannel = targetParty.commandChannel
        if (targetChannel != null) {
            requestor.sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_MEMBER_OF_COMMAND_CHANNEL).addCharName(
                    target
                )
            )
            return
        }

        // Requestor isn't a level 5 clan leader, or clan hasn't Clan Imperium skill.
        val requestorClan = requestor.clan
        if (requestorClan == null || requestorClan.leaderId != requestor.objectId || requestorClan.level < 5 || requestor.getSkill(
                391
            ) == null
        ) {
            requestor.sendPacket(SystemMessageId.COMMAND_CHANNEL_ONLY_BY_LEVEL_5_CLAN_LEADER_PARTY_LEADER)
            return
        }

        // Get the target's party leader, and do whole actions on him.
        val targetLeader = targetParty.leader
        if (!targetLeader.isProcessingRequest) {
            requestor.onTransactionRequest(targetLeader)
            targetLeader.sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.COMMAND_CHANNEL_CONFIRM_FROM_S1).addCharName(
                    requestor
                )
            )
            targetLeader.sendPacket(ExAskJoinMPCC(requestor.name))
        } else
            requestor.sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addCharName(
                    targetLeader
                )
            )
    }
}