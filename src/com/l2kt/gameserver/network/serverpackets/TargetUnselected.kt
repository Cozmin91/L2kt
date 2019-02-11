package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.Creature

class TargetUnselected(character: Creature) : L2GameServerPacket() {
    private val _targetObjId: Int = character.objectId
    private val _x: Int = character.x
    private val _y: Int = character.y
    private val _z: Int = character.z

    override fun writeImpl() {
        writeC(0x2a)
        writeD(_targetObjId)
        writeD(_x)
        writeD(_y)
        writeD(_z)
    }
}