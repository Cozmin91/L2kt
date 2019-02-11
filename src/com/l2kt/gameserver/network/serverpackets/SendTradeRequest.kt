package com.l2kt.gameserver.network.serverpackets

class SendTradeRequest(private val _senderID: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x5e)
        writeD(_senderID)
    }
}