package com.l2kt.gameserver.network.serverpackets

class AskJoinAlly(private val _requestorObjId: Int, private val _requestorName: String) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xa8)
        writeD(_requestorObjId)
        writeS(_requestorName)
    }
}