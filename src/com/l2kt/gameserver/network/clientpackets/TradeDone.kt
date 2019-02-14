package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.EnchantResult

class TradeDone : L2GameClientPacket() {
    private var _response: Int = 0

    override fun readImpl() {
        _response = readD()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        val trade = player.activeTradeList ?: return

        if (trade.isLocked)
            return

        if (_response != 1) {
            player.cancelActiveTrade()
            return
        }

        // Trade owner not found, or owner is different of packet sender.
        val owner = trade.owner
        if (owner == null || owner != player)
            return

        // Trade partner not found, cancel trade
        val partner = trade.partner
        if (partner == null || World.getInstance().getPlayer(partner.objectId) == null) {
            player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME)
            player.cancelActiveTrade()
            return
        }

        if (!player.accessLevel.allowTransaction) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
            player.cancelActiveTrade()
            return
        }

        // Sender under enchant process, close it.
        if (owner.activeEnchantItem != null) {
            owner.activeEnchantItem = null
            owner.sendPacket(EnchantResult.CANCELLED)
            owner.sendPacket(SystemMessageId.ENCHANT_SCROLL_CANCELLED)
        }

        // Partner under enchant process, close it.
        if (partner.activeEnchantItem != null) {
            partner.activeEnchantItem = null
            partner.sendPacket(EnchantResult.CANCELLED)
            partner.sendPacket(SystemMessageId.ENCHANT_SCROLL_CANCELLED)
        }

        trade.confirm()
    }
}