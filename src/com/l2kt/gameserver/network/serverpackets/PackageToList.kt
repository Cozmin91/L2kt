package com.l2kt.gameserver.network.serverpackets

/**
 * Format: (c) d[dS] d: list size [ d: char ID S: char Name ]
 * @author -Wooden-
 */
class PackageToList(private val _players: Map<Int, String>) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xC2)
        writeD(_players.size)
        for ((key, value) in _players) {
            writeD(key)
            writeS(value)
        }
    }
}