package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.item.instance.ItemInfo
import com.l2kt.gameserver.model.item.instance.ItemInstance

/**
 * @author Yme, Advi
 */
class PetInventoryUpdate @JvmOverloads constructor(private val _items: MutableList<ItemInfo> = mutableListOf()) :
    L2GameServerPacket() {

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
        writeC(0xb3)
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
        }
    }
}