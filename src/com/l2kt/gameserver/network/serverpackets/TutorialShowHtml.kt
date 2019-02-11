package com.l2kt.gameserver.network.serverpackets

class TutorialShowHtml(private val _html: String) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xa0)
        writeS(_html)
    }
}