package com.l2kt.gameserver.network.serverpackets

/**
 * format (c) dd
 */
class SetSummonRemainTime(private val _maxTime: Int, private val _remainingTime: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xd1)
        writeD(_maxTime)
        writeD(_remainingTime)
    }
}