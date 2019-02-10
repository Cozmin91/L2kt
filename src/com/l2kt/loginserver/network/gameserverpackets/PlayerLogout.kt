package com.l2kt.loginserver.network.gameserverpackets

import com.l2kt.loginserver.network.clientpackets.ClientBasePacket

class PlayerLogout(decrypt: ByteArray) : ClientBasePacket(decrypt) {
    val account: String = readS()
}