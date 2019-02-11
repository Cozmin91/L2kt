package com.l2kt.gameserver.network.loginserverpackets

class KickPlayer(decrypt: ByteArray) : LoginServerBasePacket(decrypt) {
    val account: String = readS()

}