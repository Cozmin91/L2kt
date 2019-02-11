package com.l2kt.gameserver.network.serverpackets

class TutorialCloseHtml private constructor() : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xa3)
    }

    companion object {
        val STATIC_PACKET = TutorialCloseHtml()
    }
}