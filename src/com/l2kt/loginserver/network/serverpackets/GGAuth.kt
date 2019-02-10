package com.l2kt.loginserver.network.serverpackets

class GGAuth(private val _response: Int) : L2LoginServerPacket() {

    override fun write() {
        writeC(0x0b)
        writeD(_response)
        writeD(0x00)
        writeD(0x00)
        writeD(0x00)
        writeD(0x00)
    }

    companion object {
        const val SKIP_GG_AUTH_REQUEST = 0x0b
    }
}