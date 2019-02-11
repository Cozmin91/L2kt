package com.l2kt.gameserver.network.serverpackets

/**
 * Format: (ch)
 * @author -Wooden-
 */
class ShowPCCafeCouponShowUI private constructor() : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x43)
    }

    companion object {
        val STATIC_PACKET = ShowPCCafeCouponShowUI()
    }
}