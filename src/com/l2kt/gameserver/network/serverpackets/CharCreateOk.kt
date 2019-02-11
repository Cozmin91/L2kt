package com.l2kt.gameserver.network.serverpackets

class CharCreateOk private constructor() : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x19)
        writeD(0x01)
    }

    companion object {
        val STATIC_PACKET = CharCreateOk()
    }
}