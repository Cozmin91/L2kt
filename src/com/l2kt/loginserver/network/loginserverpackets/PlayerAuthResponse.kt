package com.l2kt.loginserver.network.loginserverpackets

import com.l2kt.loginserver.network.serverpackets.ServerBasePacket

class PlayerAuthResponse(account: String, response: Boolean) : ServerBasePacket() {

    override val content: ByteArray
        get() = bytes

    init {
        writeC(0x03)
        writeS(account)
        writeC(if (response) 1 else 0)
    }
}