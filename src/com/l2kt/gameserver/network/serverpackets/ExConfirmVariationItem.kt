package com.l2kt.gameserver.network.serverpackets

/**
 * Format: (ch)ddd
 */
class ExConfirmVariationItem(private val _itemObjId: Int) : L2GameServerPacket() {
    private val _unk1: Int = 1
    private val _unk2: Int = 1

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x52)
        writeD(_itemObjId)
        writeD(_unk1)
        writeD(_unk2)
    }
}