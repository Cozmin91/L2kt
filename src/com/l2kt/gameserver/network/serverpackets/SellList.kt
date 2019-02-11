package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.item.instance.ItemInstance

class SellList(private val _money: Int, private val _items: List<ItemInstance>) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x10)
        writeD(_money)
        writeD(0x00)
        writeH(_items.size)

        for (item in _items) {
            writeH(item.item.type1)
            writeD(item.objectId)
            writeD(item.itemId)
            writeD(item.count)
            writeH(item.item.type2)
            writeH(item.customType1)
            writeD(item.item.bodyPart)
            writeH(item.enchantLevel)
            writeH(item.customType2)
            writeH(0x00)
            writeD(item.item.referencePrice / 2)
        }
    }
}