package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.Creature

/**
 * ddddddd
 */
class MoveToLocation(cha: Creature) : L2GameServerPacket() {
    private val _charObjId: Int = cha.objectId
    private val _x: Int = cha.x
    private val _y: Int = cha.y
    private val _z: Int = cha.z
    private val _xDst: Int = cha.xdestination
    private val _yDst: Int = cha.ydestination
    private val _zDst: Int = cha.zdestination

    override fun writeImpl() {
        writeC(0x01)
        writeD(_charObjId)
        writeD(_xDst)
        writeD(_yDst)
        writeD(_zDst)
        writeD(_x)
        writeD(_y)
        writeD(_z)
    }
}