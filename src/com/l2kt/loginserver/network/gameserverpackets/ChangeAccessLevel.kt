package com.l2kt.loginserver.network.gameserverpackets

import com.l2kt.loginserver.network.clientpackets.ClientBasePacket

class ChangeAccessLevel(decrypt: ByteArray) : ClientBasePacket(decrypt) {
    val level: Int = readD()
    val account: String = readS()

}