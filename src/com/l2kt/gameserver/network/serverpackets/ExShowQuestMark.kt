package com.l2kt.gameserver.network.serverpackets

/**
 * @author Luca Baldi
 */
class ExShowQuestMark(private val _questId: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x1a)
        writeD(_questId)
    }
}