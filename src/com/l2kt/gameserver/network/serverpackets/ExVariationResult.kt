package com.l2kt.gameserver.network.serverpackets

/**
 * Format: (ch)ddd
 */
class ExVariationResult(private val _stat12: Int, private val _stat34: Int, private val _unk3: Int) :
    L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x55)
        writeD(_stat12)
        writeD(_stat34)
        writeD(_unk3)
    }
}