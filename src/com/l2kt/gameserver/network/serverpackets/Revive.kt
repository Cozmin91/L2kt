package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.WorldObject

/**
 * format d
 */
class Revive(obj: WorldObject) : L2GameServerPacket() {
    private val _objectId: Int = obj.objectId

    override fun writeImpl() {
        writeC(0x07)
        writeD(_objectId)
    }
}