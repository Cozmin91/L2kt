package com.l2kt.gameserver.network.serverpackets

/**
 * Format: ch d
 * @author KenM
 */
class ExDuelReady(private val _unk1: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x4c)

        writeD(_unk1)
    }
}