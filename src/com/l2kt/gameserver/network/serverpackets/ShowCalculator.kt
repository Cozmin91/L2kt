package com.l2kt.gameserver.network.serverpackets

class ShowCalculator(private val _calculatorId: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xdc)
        writeD(_calculatorId)
    }
}