package com.l2kt.gameserver.network.serverpackets

class SendTradeDone(private val _num: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x22)
        writeD(_num)
    }
}