package com.l2kt.gameserver.network.serverpackets

/**
 * Format: (ch)ddddd
 */
class ExConfirmVariationGemstone(private val _gemstoneObjId: Int, private val _gemstoneCount: Int) :
    L2GameServerPacket() {
    private val _unk1: Int = 1
    private val _unk2: Int = 1
    private val _unk3: Int = 1

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x54)
        writeD(_gemstoneObjId)
        writeD(_unk1)
        writeD(_gemstoneCount)
        writeD(_unk2)
        writeD(_unk3)
    }
}