package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.TradeItemUpdate
import com.l2kt.gameserver.network.serverpackets.TradeOtherAdd
import com.l2kt.gameserver.network.serverpackets.TradeOwnAdd

class AddTradeItem : L2GameClientPacket() {
    private var _tradeId: Int = 0
    private var _objectId: Int = 0
    private var _count: Int = 0

    override fun readImpl() {
        _tradeId = readD()
        _objectId = readD()
        _count = readD()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        val trade = player.activeTradeList ?: return

        val partner = trade.partner
        if (partner == null || World.getPlayer(partner.objectId) == null || partner.activeTradeList == null) {
            player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME)
            player.cancelActiveTrade()
            return
        }

        if (trade.isConfirmed || partner.activeTradeList!!.isConfirmed) {
            player.sendPacket(SystemMessageId.CANNOT_ADJUST_ITEMS_AFTER_TRADE_CONFIRMED)
            return
        }

        if (!player.accessLevel.allowTransaction) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
            player.cancelActiveTrade()
            return
        }

        if (player.validateItemManipulation(_objectId) == null) {
            player.sendPacket(SystemMessageId.NOTHING_HAPPENED)
            return
        }

        val item = trade.addItem(_objectId, _count)
        if (item != null) {
            player.sendPacket(TradeOwnAdd(item))
            player.sendPacket(TradeItemUpdate(trade, player))
            trade.partner?.sendPacket(TradeOtherAdd(item))
        }
    }
}