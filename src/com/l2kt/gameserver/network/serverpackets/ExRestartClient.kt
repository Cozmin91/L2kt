package com.l2kt.gameserver.network.serverpackets

/**
 * Format: (ch)
 * @author -Wooden-
 */
class ExRestartClient private constructor() : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x47)
    }

    companion object {
        val STATIC_PACKET = ExRestartClient()
    }
}