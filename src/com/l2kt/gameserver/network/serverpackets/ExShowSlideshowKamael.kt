package com.l2kt.gameserver.network.serverpackets

/**
 * Format: ch
 * @author devScarlet & mrTJO
 */
class ExShowSlideshowKamael private constructor() : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x5b)
    }

    companion object {
        val STATIC_PACKET = ExShowSlideshowKamael()
    }
}