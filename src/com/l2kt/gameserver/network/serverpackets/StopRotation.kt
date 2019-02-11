package com.l2kt.gameserver.network.serverpackets

class StopRotation(private val _charObjId: Int, private val _degree: Int, private val _speed: Int) :
    L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x63)
        writeD(_charObjId)
        writeD(_degree)
        writeD(_speed)
        writeC(_degree)
    }
}