package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.WorldObject

/**
 * format dddd
 */
class TeleportToLocation(obj: WorldObject, private val _x: Int, private val _y: Int, private val _z: Int) :
    L2GameServerPacket() {
    private val _targetObjId: Int = obj.objectId

    override fun writeImpl() {
        writeC(0x28)
        writeD(_targetObjId)
        writeD(_x)
        writeD(_y)
        writeD(_z)
    }
}