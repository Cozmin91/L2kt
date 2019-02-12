package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.tradelist.TradeList
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.PrivateStoreManageListBuy
import com.l2kt.gameserver.network.serverpackets.PrivateStoreMsgBuy
import com.l2kt.gameserver.taskmanager.AttackStanceTaskManager

class SetPrivateStoreListBuy : L2GameClientPacket() {

    private var _items: MutableList<Item> = mutableListOf()

    override fun readImpl() {
        val count = readD()
        if (count < 1 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
            return

        for (i in 0 until count) {
            val itemId = readD()
            readH() // TODO analyse this
            readH() // TODO analyse this
            val cnt = readD()
            val price = readD()

            if (itemId < 1 || cnt < 1 || price < 0) {
                _items = mutableListOf()
                return
            }
            _items.add(i, Item(itemId, cnt, price))
        }
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        if (_items.isEmpty()) {
            player.storeType = Player.StoreType.NONE
            player.broadcastUserInfo()
            player.sendPacket(PrivateStoreManageListBuy(player))
            return
        }

        if (!player.accessLevel.allowTransaction()) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
            return
        }

        if (AttackStanceTaskManager.isInAttackStance(player) || player.isCastingNow || player.isCastingSimultaneouslyNow || player.isInDuel) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
            player.sendPacket(PrivateStoreManageListBuy(player))
            return
        }

        if (player.isInsideZone(ZoneId.NO_STORE)) {
            player.sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE)
            player.sendPacket(PrivateStoreManageListBuy(player))
            return
        }

        val tradeList = player.buyList
        tradeList.clear()

        // Check maximum number of allowed slots for pvt shops
        if (_items.size > player.privateBuyStoreLimit) {
            player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED)
            player.sendPacket(PrivateStoreManageListBuy(player))
            return
        }

        var totalCost = 0
        for (i in _items) {
            if (!i.addToTradeList(tradeList)) {
                player.sendPacket(SystemMessageId.EXCEEDED_THE_MAXIMUM)
                player.sendPacket(PrivateStoreManageListBuy(player))
                return
            }

            totalCost += i.cost.toInt()
            if (totalCost > Integer.MAX_VALUE) {
                player.sendPacket(SystemMessageId.EXCEEDED_THE_MAXIMUM)
                player.sendPacket(PrivateStoreManageListBuy(player))
                return
            }
        }

        // Check for available funds
        if (totalCost > player.adena) {
            player.sendPacket(SystemMessageId.THE_PURCHASE_PRICE_IS_HIGHER_THAN_MONEY)
            player.sendPacket(PrivateStoreManageListBuy(player))
            return
        }

        player.sitDown()
        player.storeType = Player.StoreType.BUY
        player.broadcastUserInfo()
        player.broadcastPacket(PrivateStoreMsgBuy(player))
    }

    private class Item(private val _itemId: Int, private val _count: Int, private val _price: Int) {

        val cost: Long
            get() = (_count * _price).toLong()

        fun addToTradeList(list: TradeList): Boolean {
            if (Integer.MAX_VALUE / _count < _price)
                return false

            list.addItemByItemId(_itemId, _count, _price)
            return true
        }
    }

    companion object {
        private const val BATCH_LENGTH = 16 // length of one item
    }
}