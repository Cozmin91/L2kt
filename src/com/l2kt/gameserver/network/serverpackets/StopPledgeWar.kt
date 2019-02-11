package com.l2kt.gameserver.network.serverpackets

class StopPledgeWar(private val _pledgeName: String, private val _playerName: String) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x67)
        writeS(_pledgeName)
        writeS(_playerName)
    }
}