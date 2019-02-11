package com.l2kt.gameserver.network.serverpackets

/**
 * @author chris_00 close the CommandChannel Information window
 */
class ExCloseMPCC private constructor() : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x26)
    }

    companion object {
        val STATIC_PACKET = ExCloseMPCC()
    }
}