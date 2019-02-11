package com.l2kt.gameserver.network.serverpackets

class TradePressOtherOk private constructor() : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x7c)
    }

    companion object {
        val STATIC_PACKET = TradePressOtherOk()
    }
}