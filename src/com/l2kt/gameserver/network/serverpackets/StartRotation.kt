package com.l2kt.gameserver.network.serverpackets

class StartRotation(
    private val _charObjId: Int,
    private val _degree: Int,
    private val _side: Int,
    private val _speed: Int
) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x62)
        writeD(_charObjId)
        writeD(_degree)
        writeD(_side)
        writeD(_speed)
    }
}