package com.l2kt.gameserver.network.serverpackets

class ExRegenMax(private val _count: Int, private val _time: Int, hpRegen: Double) : L2GameServerPacket() {
    private val _hpRegen: Double = hpRegen * 0.66

    override fun writeImpl() {
        writeC(0xFE)
        writeH(0x01)
        writeD(1)
        writeD(_count)
        writeD(_time)
        writeF(_hpRegen)
    }
}