package com.l2kt.gameserver.network.serverpackets

class SetupGauge : L2GameServerPacket {

    private val _color: GaugeColor
    private val _time: Int
    private val _maxTime: Int

    enum class GaugeColor {
        BLUE,
        RED,
        CYAN,
        GREEN
    }

    constructor(color: GaugeColor, time: Int) {
        _color = color
        _time = time
        _maxTime = time
    }

    constructor(color: GaugeColor, currentTime: Int, maxTime: Int) {
        _color = color
        _time = currentTime
        _maxTime = maxTime
    }

    override fun writeImpl() {
        writeC(0x6d)
        writeD(_color.ordinal)
        writeD(_time)
        writeD(_maxTime)
    }
}