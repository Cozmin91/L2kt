package com.l2kt.gameserver.network.serverpackets

class SurrenderPledgeWar(private val _pledgeName: String, private val _playerName: String) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x69)
        writeS(_pledgeName)
        writeS(_playerName)
    }
}