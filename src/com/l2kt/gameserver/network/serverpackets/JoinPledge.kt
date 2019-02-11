package com.l2kt.gameserver.network.serverpackets

class JoinPledge(private val _pledgeId: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x33)
        writeD(_pledgeId)
    }
}