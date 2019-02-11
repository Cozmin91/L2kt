package com.l2kt.gameserver.network.serverpackets

class NormalCamera private constructor() : L2GameServerPacket() {

    public override fun writeImpl() {
        writeC(0xc8)
    }

    companion object {
        val STATIC_PACKET = NormalCamera()
    }
}