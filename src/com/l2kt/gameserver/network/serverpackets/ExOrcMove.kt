package com.l2kt.gameserver.network.serverpackets

/**
 * Format: (ch)
 * @author -Wooden-
 */
class ExOrcMove private constructor() : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x44)
    }

    companion object {
        val STATIC_PACKET = ExOrcMove()
    }
}