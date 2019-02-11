package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.group.CommandChannel
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestExAcceptJoinMPCC : L2GameClientPacket() {
    private var _response: Int = 0

    override fun readImpl() {
        _response = readD()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        val requestor = player.activeRequester ?: return

        player.activeRequester = null
        requestor.onTransactionResponse()

        val requestorParty = requestor.party ?: return

        val targetParty = player.party ?: return

        if (_response == 1) {
            var channel: CommandChannel? = requestorParty.commandChannel
            if (channel == null) {
                // Consume a Strategy Guide item from requestor. If not possible, cancel the CommandChannel creation.
                if (!requestor.destroyItemByItemId("CommandChannel Creation", 8871, 1, player, true))
                    return

                channel = CommandChannel(requestorParty, targetParty)
            } else
                channel.addParty(targetParty)
        } else
            requestor.sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.S1_DECLINED_CHANNEL_INVITATION).addCharName(
                    player
                )
            )
    }
}