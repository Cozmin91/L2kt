package com.l2kt.gameserver.network.gameserverpackets

class PlayerInGame : GameServerBasePacket {

    override val content: ByteArray
        get() = bytes

    constructor(player: String) {
        writeC(0x02)
        writeH(1)
        writeS(player)
    }

    constructor(players: List<String>) {
        writeC(0x02)
        writeH(players.size)
        for (pc in players)
            writeS(pc)
    }
}