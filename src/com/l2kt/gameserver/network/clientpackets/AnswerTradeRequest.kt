package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SendTradeDone
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class AnswerTradeRequest : L2GameClientPacket() {
    private var _response: Int = 0

    override fun readImpl() {
        _response = readD()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        if (!player.accessLevel.allowTransaction()) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
            return
        }

        val partner = player.activeRequester
        if (partner == null || World.getInstance().getPlayer(partner.objectId) == null) {
            // Trade partner not found, cancel trade
            player.sendPacket(SendTradeDone(0))
            player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME)
            player.activeRequester = null
            return
        }

        if (_response == 1 && !partner.isRequestExpired)
            player.startTrade(partner)
        else
            partner.sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.S1_DENIED_TRADE_REQUEST).addCharName(
                    player
                )
            )

        // Clears requesting status
        player.activeRequester = null
        partner.onTransactionResponse()
    }
}