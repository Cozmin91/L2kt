package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Pet
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance

class GMViewItemList : L2GameServerPacket {
    private val _items: Set<ItemInstance>
    private val _limit: Int
    private val _playerName: String

    constructor(cha: Player) {
        _items = cha.inventory!!.items
        _playerName = cha.name
        _limit = cha.inventoryLimit
    }

    constructor(cha: Pet) {
        _items = cha.inventory!!.items
        _playerName = cha.name
        _limit = cha.inventoryLimit
    }

    override fun writeImpl() {
        writeC(0x94)
        writeS(_playerName)
        writeD(_limit)
        writeH(0x01) // show window ??
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