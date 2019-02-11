package com.l2kt.gameserver.network.serverpackets

/**
 * Format: ch - Trigger packet
 * @author KenM
 */
class ExShowVariationMakeWindow private constructor() : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x50)
    }

    companion object {
        val STATIC_PACKET = ExShowVariationMakeWindow()
    }
}