package com.l2kt.gameserver.network.serverpackets

import com.l2kt.Config
import com.l2kt.gameserver.model.buylist.NpcBuyList
import com.l2kt.gameserver.model.buylist.Product
import com.l2kt.gameserver.model.item.kind.Item

class ShopPreviewList(list: NpcBuyList, private val _money: Int, private val _expertise: Int) : L2GameServerPacket() {
    private val _listId: Int = list.listId
    private val _list: Collection<Product> = list.products

    override fun writeImpl() {
        writeC(0xef)
        writeC(0xc0) // ?
        writeC(0x13) // ?
        writeC(0x00) // ?
        writeC(0x00) // ?
        writeD(_money) // current money
        writeD(_listId)

        var newlength = 0
        for (product in _list) {
            if (product.item.crystalType.id <= _expertise && product.item.isEquipable)
                newlength++
        }
        writeH(newlength)

        for (product in _list) {
            if (product.item.crystalType.id <= _expertise && product.item.isEquipable) {
                writeD(product.itemId)
                writeH(product.item.type2) // item type2

                if (product.item.type1 != Item.TYPE1_ITEM_QUESTITEM_ADENA)
                    writeH(product.item.bodyPart) // slot
                else
                    writeH(0x00) // slot

                writeD(Config.WEAR_PRICE)
            }
        }
    }
}