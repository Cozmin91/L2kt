package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.item.instance.ItemInstance

/**
 * @author -Wooden-
 */
class PackageSendableList(private val _items: Array<ItemInstance>, private val _playerObjId: Int) :
    L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xC3)
        writeD(_playerObjId)
        writeD(client.activeChar?.adena ?: 0)
        writeD(_items.size)

        for (temp in _items) {
            if (temp.item == null)
                continue

            val item = temp.item

            writeH(item.type1)
            writeD(temp.objectId)
            writeD(temp.itemId)
            writeD(temp.count)
            writeH(item.type2)
            writeH(temp.customType1)
            writeD(item.bodyPart)
            writeH(temp.enchantLevel)
            writeH(temp.customType2)
            writeH(0x00)
            writeD(temp.objectId)
        }
    }
}