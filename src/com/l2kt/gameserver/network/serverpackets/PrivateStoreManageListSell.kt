package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.tradelist.TradeItem

class PrivateStoreManageListSell(player: Player, isPackageSale: Boolean) : L2GameServerPacket() {
    private val _objId: Int = player.objectId
    private val _playerAdena: Int = player.adena
    private val _packageSale: Boolean
    private val _itemList: Array<TradeItem>
    private val _sellList: List<TradeItem>

    init {

        player.sellList.updateItems()

        _packageSale = if (player.sellList.isPackaged) true else isPackageSale
        _itemList = player.inventory!!.getAvailableItems(player.sellList)
        _sellList = player.sellList.items
    }

    override fun writeImpl() {
        writeC(0x9a)
        writeD(_objId)
        writeD(if (_packageSale) 1 else 0)
        writeD(_playerAdena)

        writeD(_itemList.size)
        for (item in _itemList) {
            writeD(item.item.type2)
            writeD(item.objectId)
            writeD(item.item.itemId)
            writeD(item.count)
            writeH(0x00)
            writeH(item.enchant)
            writeH(0x00)
            writeD(item.item.bodyPart)
            writeD(item.price)
        }

        writeD(_sellList.size)
        for (item in _sellList) {
            writeD(item.item.type2)
            writeD(item.objectId)
            writeD(item.item.itemId)
            writeD(item.count)
            writeH(0x00)
            writeH(item.enchant)
            writeH(0x00)
            writeD(item.item.bodyPart)
            writeD(item.price)
            writeD(item.item.referencePrice)
        }
    }
}