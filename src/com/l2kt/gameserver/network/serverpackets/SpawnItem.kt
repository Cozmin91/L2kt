package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.item.instance.ItemInstance

class SpawnItem : L2GameServerPacket {
    private val _objectId: Int
    private val _itemId: Int
    private val _x: Int
    private val _y: Int
    private val _z: Int
    private val _stackable: Int
    private val _count: Int

    constructor(item: ItemInstance) {
        _objectId = item.objectId
        _itemId = item.itemId
        _x = item.x
        _y = item.y
        _z = item.z
        _stackable = if (item.isStackable) 0x01 else 0x00
        _count = item.count
    }

    constructor(`object`: WorldObject) {
        _objectId = `object`.objectId
        _itemId = `object`.polyId
        _x = `object`.x
        _y = `object`.y
        _z = `object`.z
        _stackable = 0x00
        _count = 1
    }

    override fun writeImpl() {
        writeC(0x0b)
        writeD(_objectId)
        writeD(_itemId)
        writeD(_x)
        writeD(_y)
        writeD(_z)
        writeD(_stackable)
        writeD(_count)
        writeD(0x00) // c2
    }
}