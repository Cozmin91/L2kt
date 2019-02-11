package com.l2kt.gameserver.network.serverpackets

class ChairSit(private val _playerId: Int, private val _staticId: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xe1)
        writeD(_playerId)
        writeD(_staticId)
    }
}