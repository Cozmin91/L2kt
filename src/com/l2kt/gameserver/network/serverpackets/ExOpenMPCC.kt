package com.l2kt.gameserver.network.serverpackets

/**
 * @author chris_00 opens the CommandChannel Information window
 */
class ExOpenMPCC private constructor() : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x25)
    }

    companion object {
        val STATIC_PACKET = ExOpenMPCC()
    }
}