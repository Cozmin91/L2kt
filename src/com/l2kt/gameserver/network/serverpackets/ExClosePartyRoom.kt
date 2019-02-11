package com.l2kt.gameserver.network.serverpackets

/**
 * @author Gnacik
 */
class ExClosePartyRoom private constructor() : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x0f)
    }

    companion object {
        val STATIC_PACKET = ExClosePartyRoom()
    }
}