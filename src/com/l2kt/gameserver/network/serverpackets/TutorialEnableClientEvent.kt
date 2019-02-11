package com.l2kt.gameserver.network.serverpackets

class TutorialEnableClientEvent(private val _eventId: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xa2)
        writeD(_eventId)
    }
}