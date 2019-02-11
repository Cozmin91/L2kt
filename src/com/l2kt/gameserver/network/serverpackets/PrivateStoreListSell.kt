package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.tradelist.TradeItem

class PrivateStoreListSell(player: Player, private val _storePlayer: Player) : L2GameServerPacket() {
    private val _playerAdena: Int = player.adena
    private val _items: List<TradeItem> = _storePlayer.sellList.items
    private val _packageSale: Boolean = _storePlayer.sellList.isPackaged

    override fun writeImpl() {
        writeC(0x9b)
        writeD(_storePlayer.objectId)
        writeD(if (_packageSale) 1 else 0)
        writeD(_playerAdena)
        writeD(_items.size)

        for (item in _items) {
            writeD(item.item.type2)
            writeD(item.objectId)
            writeD(item.item.itemId)
            writeD(item.count)
            writeH(0x00)
            writeH(item.enchant)
            writeH(0x00)
            writeD(item.item.bodyPart)
            writeD(item.price) // your price
            writeD(item.item.referencePrice) // store price
        }
    }
}