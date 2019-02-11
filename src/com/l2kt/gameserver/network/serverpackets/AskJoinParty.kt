package com.l2kt.gameserver.network.serverpackets

class AskJoinParty(private val _requestorName: String, private val _itemDistribution: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x39)
        writeS(_requestorName)
        writeD(_itemDistribution)
    }
}