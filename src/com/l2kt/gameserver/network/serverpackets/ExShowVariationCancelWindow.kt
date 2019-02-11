package com.l2kt.gameserver.network.serverpackets

/**
 * Format: ch - Trigger packet
 * @author KenM
 */
class ExShowVariationCancelWindow private constructor() : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x51)
    }

    companion object {
        val STATIC_PACKET = ExShowVariationCancelWindow()
    }
}