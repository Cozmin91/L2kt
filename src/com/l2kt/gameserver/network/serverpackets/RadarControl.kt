package com.l2kt.gameserver.network.serverpackets

/**
 * 0xEB RadarControl ddddd
 */
class RadarControl(
    private val _showRadar: Int,
    private val _type: Int,
    private val _x: Int,
    private val _y: Int,
    private val _z: Int
)// 0 = showradar; 1 = delete radar;
    : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xEB)
        writeD(_showRadar)
        writeD(_type)
        writeD(_x)
        writeD(_y)
        writeD(_z)
    }
}