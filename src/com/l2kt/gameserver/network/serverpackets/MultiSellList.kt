package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.data.xml.MultisellData
import com.l2kt.gameserver.model.multisell.ListContainer

class MultiSellList(private val _list: ListContainer, private var _index: Int) : L2GameServerPacket() {
    private var _size: Int = 0

    private var _finished: Boolean = false

    init {

        _size = _list.entries.size - _index
        if (_size > MultisellData.PAGE_SIZE) {
            _finished = false
            _size = MultisellData.PAGE_SIZE
        } else
            _finished = true
    }

    override fun writeImpl() {
        writeC(0xd0)
        writeD(_list.id) // list id
        writeD(1 + _index / MultisellData.PAGE_SIZE) // page
        writeD(if (_finished) 1 else 0) // finished
        writeD(MultisellData.PAGE_SIZE) // size of pages
        writeD(_size) // list lenght

        while (_size-- > 0) {
            val ent = _list.entries[_index++]

            writeD(_index)
            writeD(0x00) // C6
            writeD(0x00) // C6
            writeC(if (ent.isStackable) 1 else 0)
            writeH(ent.products.size)
            writeH(ent.ingredients.size)

            for (ing in ent.products) {
                writeH(ing.itemId)
                if (ing.template != null) {
                    writeD(ing.template.bodyPart)
                    writeH(ing.template.type2)
                } else {
                    writeD(0)
                    writeH(65535)
                }
                writeD(ing.itemCount)
                writeH(ing.enchantLevel)
                writeD(0x00) // TODO: i.getAugmentId()
                writeD(0x00) // TODO: i.getManaLeft()
            }

            for (ing in ent.ingredients) {
                writeH(ing.itemId)
                writeH(if (ing.template != null) ing.template.type2 else 65535)
                writeD(ing.itemCount)
                writeH(ing.enchantLevel)
                writeD(0x00) // TODO: i.getAugmentId()
                writeD(0x00) // TODO: i.getManaLeft()
            }
        }
    }
}