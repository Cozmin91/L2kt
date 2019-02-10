package com.l2kt.loginserver.network.loginserverpackets

import com.l2kt.loginserver.network.serverpackets.ServerBasePacket

class KickPlayer(account: String) : ServerBasePacket() {

    override val content: ByteArray
        get() = bytes

    init {
        writeC(0x04)
        writeS(account)
    }
}