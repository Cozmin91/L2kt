package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.tradelist.TradeItem

class PrivateStoreManageListBuy(player: Player) : L2GameServerPacket() {
    private val _objId: Int = player.objectId
    private val _playerAdena: Int = player.adena
    private val _itemList: Array<ItemInstance> = player.inventory!!.getUniqueItems(false, true)
    private val _buyList: List<TradeItem> = player.buyList.items

    override fun writeImpl() {
        writeC(0xb7)
        writeD(_objId)
        writeD(_playerAdena)

        writeD(_itemList.size) // inventory items for potential buy
        for (item in _itemList) {
            writeD(item.itemId)
            writeH(item.enchantLevel)
            writeD(item.count)
            writeD(item.referencePrice)
            writeH(0x00)
            writeD(item.item.bodyPart)
            writeH(item.item.type2)
        }

        writeD(_buyList.size) // count for all items already added for buy
        for (item in _buyList) {
            writeD(item.item.itemId)
            writeH(item.enchant)
            writeD(item.count)
            writeD(item.item.referencePrice)
            writeH(0x00)
            writeD(item.item.bodyPart)
            writeH(item.item.type2)
            writeD(item.price)// your price
            writeD(item.item.referencePrice)// fixed store price
        }
    }
}