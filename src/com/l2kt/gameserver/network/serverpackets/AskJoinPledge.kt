package com.l2kt.gameserver.network.serverpackets

class AskJoinPledge(private val _requestorObjId: Int, private val _pledgeName: String) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x32)
        writeD(_requestorObjId)
        writeS(_pledgeName)
    }
}