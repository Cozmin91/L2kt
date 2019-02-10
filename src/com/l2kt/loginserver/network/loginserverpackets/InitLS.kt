package com.l2kt.loginserver.network.loginserverpackets

import com.l2kt.loginserver.LoginServer
import com.l2kt.loginserver.network.serverpackets.ServerBasePacket

class InitLS(publickey: ByteArray) : ServerBasePacket() {

    override val content: ByteArray
        get() = bytes

    init {
        writeC(0x00)
        writeD(LoginServer.PROTOCOL_REV)
        writeD(publickey.size)
        writeB(publickey)
    }
}