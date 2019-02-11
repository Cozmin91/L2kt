package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.tradelist.TradeItem

class PrivateStoreListBuy(player: Player, private val _storePlayer: Player) : L2GameServerPacket() {
    private val _playerAdena: Int
    private val _items: List<TradeItem>

    init {
        _storePlayer.sellList.updateItems()

        _playerAdena = player.adena
        _items = _storePlayer.buyList.getAvailableItems(player.inventory)
    }

    override fun writeImpl() {
        writeC(0xb8)
        writeD(_storePlayer.objectId)
        writeD(_playerAdena)
        writeD(_items.size)

        for (item in _items) {
            writeD(item.objectId)
            writeD(item.item.itemId)
            writeH(item.enchant)
            writeD(item.count) // give max possible sell amount

            writeD(item.item.referencePrice)
            writeH(0)

            writeD(item.item.bodyPart)
            writeH(item.item.type2)
            writeD(item.price)// buyers price

            writeD(item.count) // maximum possible tradecount
        }
    }
}