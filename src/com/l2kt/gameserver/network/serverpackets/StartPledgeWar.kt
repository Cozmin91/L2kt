package com.l2kt.gameserver.network.serverpackets

class StartPledgeWar(private val _pledgeName: String, private val _playerName: String) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x65)
        writeS(_playerName)
        writeS(_pledgeName)
    }
}