package com.l2kt.gameserver.network.serverpackets

class TutorialShowQuestionMark(private val _markId: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xa1)
        writeD(_markId)
    }
}