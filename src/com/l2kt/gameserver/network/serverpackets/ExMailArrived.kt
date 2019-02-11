package com.l2kt.gameserver.network.serverpackets

/**
 * Format: (ch) (just a trigger)
 * @author -Wooden-
 */
class ExMailArrived private constructor() : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x2d)
    }

    companion object {
        val STATIC_PACKET = ExMailArrived()
    }
}