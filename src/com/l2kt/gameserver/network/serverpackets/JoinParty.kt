package com.l2kt.gameserver.network.serverpackets

/**
 * format cd
 */
class JoinParty(private val _response: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x3a)
        writeD(_response)
    }
}