package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.tradelist.TradeItem

/**
 * @author Yme
 */
class TradeOtherAdd(private val _item: TradeItem) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x21)

        writeH(1) // item count

        writeH(_item.item.type1) // item type1
        writeD(_item.objectId)
        writeD(_item.item.itemId)
        writeD(_item.count)
        writeH(_item.item.type2) // item type2
        writeH(0x00) // ?

        writeD(_item.item.bodyPart) // rev 415 slot 0006-lr.ear 0008-neck 0030-lr.finger 0040-head 0080-?? 0100-l.hand 0200-gloves 0400-chest 0800-pants 1000-feet 2000-?? 4000-r.hand 8000-r.hand
        writeH(_item.enchant) // enchant level
        writeH(0x00) // ?
        writeH(0x00)
    }
}
