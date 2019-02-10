package com.l2kt.loginserver.network.gameserverpackets

import com.l2kt.loginserver.network.SessionKey
import com.l2kt.loginserver.network.clientpackets.ClientBasePacket

class PlayerAuthRequest(decrypt: ByteArray) : ClientBasePacket(decrypt) {
    val account: String
    val key: SessionKey

    init {
        account = readS()
        val playKey1 = readD()
        val playKey2 = readD()
        val loginKey1 = readD()
        val loginKey2 = readD()

        key = SessionKey(loginKey1, loginKey2, playKey1, playKey2)
    }
}