package com.l2kt.gameserver.network.gameserverpackets

class AuthRequest(
    id: Int,
    acceptAlternate: Boolean,
    hexid: ByteArray,
    host: String,
    port: Int,
    reserveHost: Boolean,
    maxplayer: Int
) : GameServerBasePacket() {
    override val content: ByteArray
        get() = bytes

    init {
        writeC(0x01)
        writeC(id)
        writeC(if (acceptAlternate) 0x01 else 0x00)
        writeC(if (reserveHost) 0x01 else 0x00)
        writeS(host)
        writeH(port)
        writeD(maxplayer)
        writeD(hexid.size)
        writeB(hexid)
    }
}