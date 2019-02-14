package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.tradelist.TradeList
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.PrivateStoreManageListSell
import com.l2kt.gameserver.network.serverpackets.PrivateStoreMsgSell
import com.l2kt.gameserver.taskmanager.AttackStanceTaskManager

class SetPrivateStoreListSell : L2GameClientPacket() {

    private var _packageSale: Boolean = false
    private var _items: MutableList<Item> = mutableListOf()

    override fun readImpl() {
        _packageSale = readD() == 1
        val count = readD()
        if (count < 1 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
            return

        for (i in 0 until count) {
            val itemId = readD()
            val cnt = readD().toLong()
            val price = readD()

            if (itemId < 1 || cnt < 1 || price < 0) {
                _items = mutableListOf()
                return
            }
            _items.add(i, Item(itemId, cnt.toInt(), price))
        }
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        if (_items.isEmpty()) {
            player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS)
            player.storeType = Player.StoreType.NONE
            player.broadcastUserInfo()
            player.sendPacket(PrivateStoreManageListSell(player, _packageSale))
            return
        }

        if (!player.accessLevel.allowTransaction) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
            return
        }

        if (AttackStanceTaskManager.isInAttackStance(player) || player.isCastingNow || player.isCastingSimultaneouslyNow || player.isInDuel) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
            player.sendPacket(PrivateStoreManageListSell(player, _packageSale))
            return
        }

        if (player.isInsideZone(ZoneId.NO_STORE)) {
            player.sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE)
            player.sendPacket(PrivateStoreManageListSell(player, _packageSale))
            return
        }

        // Check maximum number of allowed slots for pvt shops
        if (_items.size > player.privateSellStoreLimit) {
            player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED)
            player.sendPacket(PrivateStoreManageListSell(player, _packageSale))
            return
        }

        val tradeList = player.sellList
        tradeList.clear()
        tradeList.isPackaged = _packageSale

        var totalCost = player.adena
        for (i in _items) {
            if (!i.addToTradeList(tradeList)) {
                player.sendPacket(SystemMessageId.EXCEEDED_THE_MAXIMUM)
                player.sendPacket(PrivateStoreManageListSell(player, _packageSale))
                return
            }

            totalCost += i.price.toInt()
            if (totalCost > Integer.MAX_VALUE) {
                player.sendPacket(SystemMessageId.EXCEEDED_THE_MAXIMUM)
                player.sendPacket(PrivateStoreManageListSell(player, _packageSale))
                return
            }
        }

        player.sitDown()
        player.storeType = if (_packageSale) Player.StoreType.PACKAGE_SELL else Player.StoreType.SELL
        player.broadcastUserInfo()
        player.broadcastPacket(PrivateStoreMsgSell(player))
    }

    private class Item(private val _itemId: Int, private val _count: Int, private val _price: Int) {

        val price: Long
            get() = (_count * _price).toLong()

        fun addToTradeList(list: TradeList): Boolean {
            if (Integer.MAX_VALUE / _count < _price)
                return false

            list.addItem(_itemId, _count, _price)
            return true
        }
    }

    companion object {
        private const val BATCH_LENGTH = 12 // length of the one item
    }
}