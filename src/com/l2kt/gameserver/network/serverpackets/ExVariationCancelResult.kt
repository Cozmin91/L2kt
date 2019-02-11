package com.l2kt.gameserver.network.serverpackets

/**
 * Format: (ch)ddd
 */
class ExVariationCancelResult(private val _unk1: Int) : L2GameServerPacket() {
    private val _closeWindow: Int = 1

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x57)
        writeD(_closeWindow)
        writeD(_unk1)
    }
}