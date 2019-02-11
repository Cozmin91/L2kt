package com.l2kt.gameserver.network.serverpackets

/**
 * @author GodKratos
 */
class ExOlympiadMatchEnd private constructor() : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x2c)
    }

    companion object {
        val STATIC_PACKET = ExOlympiadMatchEnd()
    }
}