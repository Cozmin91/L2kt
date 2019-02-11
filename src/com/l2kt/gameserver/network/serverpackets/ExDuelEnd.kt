package com.l2kt.gameserver.network.serverpackets

/**
 * Format: ch d
 * @author KenM
 */
class ExDuelEnd(private val _unk1: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x4e)

        writeD(_unk1)
    }
}