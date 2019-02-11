package com.l2kt.gameserver.network.serverpackets

class SunRise private constructor() : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x1c)
    }

    companion object {
        val STATIC_PACKET = SunRise()
    }
}
