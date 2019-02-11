package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.item.instance.ItemInfo
import com.l2kt.gameserver.model.item.instance.ItemInstance
import java.util.*

/**
 * @author Advi
 */
class InventoryUpdate : L2GameServerPacket {
    private var _items: MutableList<ItemInfo>

    constructor() {
        _items = ArrayList()
    }

    constructor(items: MutableList<ItemInfo>) {
        _items = items
    }

    fun addItem(item: ItemInstance?) {
        if (item != null)
            _items.add(ItemInfo(item))
    }

    fun addNewItem(item: ItemInstance?) {
        if (item != null)
            _items.add(ItemInfo(item, ItemInstance.ItemState.ADDED))
    }

    fun addModifiedItem(item: ItemInstance?) {
        if (item != null)
            _items.add(ItemInfo(item, ItemInstance.ItemState.MODIFIED))
    }

    fun addRemovedItem(item: ItemInstance?) {
        if (item != null)
            _items.add(ItemInfo(item, ItemInstance.ItemState.REMOVED))
    }

    fun addItems(items: List<ItemInstance>?) {
        if (items != null)
            for (item in items)
                _items.add(ItemInfo(item))
    }

    override fun writeImpl() {
        writeC(0x27)
        writeH(_items.size)

        for (temp in _items) {
            val item = temp.item

            writeH(temp.change.ordinal)
            writeH(item.type1)
            writeD(temp.objectId)
            writeD(item.itemId)
            writeD(temp.count)
            writeH(item.type2)
            writeH(temp.customType1)
            writeH(temp.equipped)
            writeD(item.bodyPart)
            writeH(temp.enchant)
            writeH(temp.customType2)
            writeD(temp.augmentationBoni)
            writeD(temp.mana)
        }
    }
}