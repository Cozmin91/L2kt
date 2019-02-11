package com.l2kt.gameserver.network.serverpackets

/**
 * @author chris_00 Asks the player to join a CC
 */
class ExAskJoinMPCC(private val _requestorName: String) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xFE)
        writeH(0x27)
        writeS(_requestorName)
    }
}