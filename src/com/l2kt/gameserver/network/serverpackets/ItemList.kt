package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance

class ItemList(cha: Player, private val _showWindow: Boolean) : L2GameServerPacket() {
    private val _items: Set<ItemInstance> = cha.inventory.items

    override fun writeImpl() {
        writeC(0x1b)
        writeH(if (_showWindow) 0x01 else 0x00)
        writeH(_items.size)

        for (temp in _items) {
            val item = temp.item

            writeH(item.type1)
            writeD(temp.objectId)
            writeD(temp.itemId)
            writeD(temp.count)
            writeH(item.type2)
            writeH(temp.customType1)
            writeH(if (temp.isEquipped) 0x01 else 0x00)
            writeD(item.bodyPart)
            writeH(temp.enchantLevel)
            writeH(temp.customType2)
            writeD(if (temp.isAugmented) temp.augmentation.augmentationId else 0x00)
            writeD(temp.mana)
        }
    }
}