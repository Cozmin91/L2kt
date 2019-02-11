package com.l2kt.gameserver.network.serverpackets

/**
 * @author Luca Baldi
 */
class ExQuestInfo private constructor() : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x19)
    }

    companion object {
        val STATIC_PACKET = ExQuestInfo()
    }
}