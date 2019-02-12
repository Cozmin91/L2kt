package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestExOustFromMPCC : L2GameClientPacket() {
    private var _name: String = ""

    override fun readImpl() {
        _name = readS()
    }

    override fun runImpl() {
        val requestor = client.activeChar ?: return

        val target = World.getInstance().getPlayer(_name)
        if (target == null) {
            requestor.sendPacket(SystemMessageId.TARGET_CANT_FOUND)
            return
        }

        if (requestor == target) {
            requestor.sendPacket(SystemMessageId.INCORRECT_TARGET)
            return
        }

        val requestorParty = requestor.party
        val targetParty = target.party

        if (requestorParty == null || targetParty == null) {
            requestor.sendPacket(SystemMessageId.INCORRECT_TARGET)
            return
        }

        val requestorChannel = requestorParty.commandChannel
        if (requestorChannel == null || !requestorChannel.isLeader(requestor)) {
            requestor.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
            return
        }

        if (!requestorChannel.removeParty(targetParty)) {
            requestor.sendPacket(SystemMessageId.INCORRECT_TARGET)
            return
        }

        targetParty.broadcastMessage(SystemMessageId.DISMISSED_FROM_COMMAND_CHANNEL)

        // check if CC has not been canceled
        if (requestorParty.isInCommandChannel)
            requestorParty.commandChannel.broadcastPacket(
                SystemMessage.getSystemMessage(SystemMessageId.S1_PARTY_DISMISSED_FROM_COMMAND_CHANNEL).addCharName(
                    targetParty.leader
                )
            )
    }
}