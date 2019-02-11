package com.l2kt.gameserver.network.serverpackets

import com.l2kt.Config
import com.l2kt.gameserver.model.buylist.NpcBuyList
import com.l2kt.gameserver.model.buylist.Product

class BuyList(list: NpcBuyList, private val _money: Int, taxRate: Double) : L2GameServerPacket() {
    private val _listId: Int = list.listId
    private val _list: Collection<Product> = list.products
    private var _taxRate = taxRate

    override fun writeImpl() {
        writeC(0x11)
        writeD(_money)
        writeD(_listId)
        writeH(_list.size)

        for (product in _list) {
            if (product.count > 0 || !product.hasLimitedStock()) {
                writeH(product.item.type1)
                writeD(product.itemId)
                writeD(product.itemId)
                writeD(if (product.count < 0) 0 else product.count)
                writeH(product.item.type2)
                writeH(0x00) // TODO: ItemInstance getCustomType1()
                writeD(product.item.bodyPart)
                writeH(0x00) // TODO: ItemInstance getEnchantLevel()
                writeH(0x00) // TODO: ItemInstance getCustomType2()
                writeH(0x00)

                if (product.itemId in 3960..4026)
                    writeD((product.price.toDouble() * Config.RATE_SIEGE_GUARDS_PRICE * (1 + _taxRate)).toInt())
                else
                    writeD((product.price * (1 + _taxRate)).toInt())
            }
        }
    }
}