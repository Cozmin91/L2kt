package com.l2kt.gameserver.network.serverpackets

class PartySmallWindowDeleteAll private constructor() : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x50)
    }

    companion object {
        val STATIC_PACKET = PartySmallWindowDeleteAll()
    }
}