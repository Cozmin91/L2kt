package com.l2kt.gameserver.network.gameserverpackets

class PlayerLogout(player: String) : GameServerBasePacket() {

    override val content: ByteArray
        get() = bytes

    init {
        writeC(0x03)
        writeS(player)
    }
}