package com.l2kt.gameserver.network.serverpackets

class SunSet private constructor() : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x1d)
    }

    companion object {
        val STATIC_PACKET = SunSet()
    }
}