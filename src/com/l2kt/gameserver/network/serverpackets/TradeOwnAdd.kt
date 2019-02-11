package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.tradelist.TradeItem

class TradeOwnAdd(private val _item: TradeItem) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x20)

        writeH(1) // item count

        writeH(_item.item.type1)
        writeD(_item.objectId)
        writeD(_item.item.itemId)
        writeD(_item.count)
        writeH(_item.item.type2)
        writeH(0x00) // ?

        writeD(_item.item.bodyPart)
        writeH(_item.enchant)
        writeH(0x00) // ?
        writeH(0x00)
    }
}