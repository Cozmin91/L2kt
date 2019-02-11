package com.l2kt.gameserver.network.serverpackets

class ExFishingEnd(private val _win: Boolean, private val _playerId: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x14)
        writeD(_playerId)
        writeC(if (_win) 1 else 0)
    }
}