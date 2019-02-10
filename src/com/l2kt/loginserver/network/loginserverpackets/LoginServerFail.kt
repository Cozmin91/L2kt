package com.l2kt.loginserver.network.loginserverpackets

import com.l2kt.loginserver.network.serverpackets.ServerBasePacket

class LoginServerFail(reason: Int) : ServerBasePacket() {

    override val content: ByteArray
        get() = bytes

    init {
        writeC(0x01)
        writeC(reason)
    }

    companion object {
        const val REASON_IP_BANNED = 1
        const val REASON_IP_RESERVED = 2
        const val REASON_WRONG_HEXID = 3
        const val REASON_ID_RESERVED = 4
        const val REASON_NO_FREE_ID = 5
        const val NOT_AUTHED = 6
        const val REASON_ALREADY_LOGGED_IN = 7
    }
}