package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.Creature

class ChangeWaitType(character: Creature, private val _moveType: Int) : L2GameServerPacket() {

    private val _charObjId: Int = character.objectId
    private val _x: Int = character.x
    private val _y: Int = character.y
    private val _z: Int = character.z

    override fun writeImpl() {
        writeC(0x2f)
        writeD(_charObjId)
        writeD(_moveType)
        writeD(_x)
        writeD(_y)
        writeD(_z)
    }

    companion object {
        const val WT_SITTING = 0
        const val WT_STANDING = 1
        const val WT_START_FAKEDEATH = 2
        const val WT_STOP_FAKEDEATH = 3
    }
}