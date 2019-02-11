package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.Creature

/**
 * format ddddd
 */
class StopMove(
    private val _objectId: Int,
    private val _x: Int,
    private val _y: Int,
    private val _z: Int,
    private val _heading: Int
) : L2GameServerPacket() {

    constructor(cha: Creature) : this(cha.objectId, cha.x, cha.y, cha.z, cha.heading) {}

    override fun writeImpl() {
        writeC(0x47)
        writeD(_objectId)
        writeD(_x)
        writeD(_y)
        writeD(_z)
        writeD(_heading)
    }
}