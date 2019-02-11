package com.l2kt.gameserver.network.serverpackets

/**
 * format cdd
 */
class FriendAddRequest(private val _requestorName: String) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x7d)
        writeS(_requestorName)
        writeD(0)
    }
}