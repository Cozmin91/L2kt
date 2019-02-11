package com.l2kt.gameserver.network.serverpackets

/**
 * Format: (ch)ddddd
 */
class ExConfirmVariationRefiner(
    private val _refinerItemObjId: Int,
    private val _lifestoneItemId: Int,
    private val _gemstoneItemId: Int,
    private val _gemstoneCount: Int
) : L2GameServerPacket() {
    private val _unk2: Int = 1

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x53)
        writeD(_refinerItemObjId)
        writeD(_lifestoneItemId)
        writeD(_gemstoneItemId)
        writeD(_gemstoneCount)
        writeD(_unk2)
    }
}