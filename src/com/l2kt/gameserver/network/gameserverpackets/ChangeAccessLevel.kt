package com.l2kt.gameserver.network.gameserverpackets

class ChangeAccessLevel(player: String, access: Int) : GameServerBasePacket() {
    override val content: ByteArray
        get() = bytes

    init {
        writeC(0x04)
        writeD(access)
        writeS(player)
    }
}