package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.item.instance.ItemInstance

/**
 * format ddddd
 */
class GetItem(private val _item: ItemInstance, private val _playerId: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x0d)
        writeD(_playerId)
        writeD(_item.objectId)

        writeD(_item.x)
        writeD(_item.y)
        writeD(_item.z)
    }
}