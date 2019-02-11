package com.l2kt.gameserver.network.serverpackets

class CharDeleteOk private constructor() : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x23)
    }

    companion object {
        val STATIC_PACKET = CharDeleteOk()
    }
}