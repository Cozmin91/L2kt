package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.Creature

class FinishRotation(cha: Creature) : L2GameServerPacket() {
    private val _heading: Int = cha.heading
    private val _charObjId: Int = cha.objectId

    override fun writeImpl() {
        writeC(0x63)
        writeD(_charObjId)
        writeD(_heading)
    }
}