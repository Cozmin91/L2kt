package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.item.instance.ItemInstance

class DropItem(private val _item: ItemInstance, private val _charObjId: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x0c)
        writeD(_charObjId)
        writeD(_item.objectId)
        writeD(_item.itemId)

        writeD(_item.x)
        writeD(_item.y)
        writeD(_item.z)

        if (_item.isStackable)
            writeD(0x01)
        else
            writeD(0x00)
        writeD(_item.count)

        writeD(1) // unknown
    }
}