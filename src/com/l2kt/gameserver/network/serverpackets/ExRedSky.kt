package com.l2kt.gameserver.network.serverpackets

/**
 * Format: ch d
 * @author KenM
 */
class ExRedSky(private val _duration: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xFE)
        writeH(0x40)
        writeD(_duration)
    }
}