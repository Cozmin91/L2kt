package com.l2kt.gameserver.network.serverpackets

class Earthquake(
    private val _x: Int,
    private val _y: Int,
    private val _z: Int,
    private val _intensity: Int,
    private val _duration: Int
) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xc4)
        writeD(_x)
        writeD(_y)
        writeD(_z)
        writeD(_intensity)
        writeD(_duration)
        writeD(0x00) // Unknown
    }
}