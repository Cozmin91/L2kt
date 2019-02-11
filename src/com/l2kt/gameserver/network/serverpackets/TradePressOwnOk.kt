package com.l2kt.gameserver.network.serverpackets

class TradePressOwnOk private constructor() : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x75)
    }

    companion object {
        val STATIC_PACKET = TradePressOwnOk()
    }
}