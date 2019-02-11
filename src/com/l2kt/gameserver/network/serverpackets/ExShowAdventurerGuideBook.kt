package com.l2kt.gameserver.network.serverpackets

/**
 * Format: ch (trigger)
 * @author KenM
 */
class ExShowAdventurerGuideBook private constructor() : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xFE)
        writeH(0x37)
    }

    companion object {
        val STATIC_PACKET = ExShowAdventurerGuideBook()
    }
}