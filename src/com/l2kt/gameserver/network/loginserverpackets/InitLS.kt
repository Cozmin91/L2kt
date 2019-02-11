package com.l2kt.gameserver.network.loginserverpackets

class InitLS(decrypt: ByteArray) : LoginServerBasePacket(decrypt) {
    val revision: Int
    val rsaKey: ByteArray

    init {

        revision = readD()
        val size = readD()
        rsaKey = readB(size)
    }
}