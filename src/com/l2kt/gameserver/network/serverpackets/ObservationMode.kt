package com.l2kt.gameserver.network.serverpackets

class ObservationMode(private val _x: Int, private val _y: Int, private val _z: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xdf)
        writeD(_x)
        writeD(_y)
        writeD(_z)
        writeC(0x00)
        writeC(0xc0)
        writeC(0x00)
    }
}