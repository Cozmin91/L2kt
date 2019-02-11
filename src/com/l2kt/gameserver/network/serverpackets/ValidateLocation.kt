package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.Creature

class ValidateLocation(cha: Creature) : L2GameServerPacket() {
    private val _charObjId: Int = cha.objectId
    private val _x: Int = cha.x
    private val _y: Int = cha.y
    private val _z: Int = cha.z
    private val _heading: Int = cha.heading

    override fun writeImpl() {
        writeC(0x61)
        writeD(_charObjId)
        writeD(_x)
        writeD(_y)
        writeD(_z)
        writeD(_heading)
    }
}