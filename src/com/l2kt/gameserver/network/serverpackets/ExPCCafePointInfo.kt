package com.l2kt.gameserver.network.serverpackets

/**
 * Format: ch ddcdc
 * @author KenM
 */
class ExPCCafePointInfo(
    private val _score: Int,
    modify: Int,
    addPoint: Boolean,
    pointType: Boolean,
    private val _remainingTime: Int
) : L2GameServerPacket() {
    private val _modify: Int = if (addPoint) modify else modify * -1
    private val _periodType = 1
    private var _pointType = if (addPoint) if (pointType) 0 else 1 else 2

    override fun writeImpl() {
        writeC(0xFE)
        writeH(0x31)
        writeD(_score)
        writeD(_modify)
        writeC(_periodType)
        writeD(_remainingTime)
        writeC(_pointType)
    }
}