package com.l2kt.gameserver.network.loginserverpackets

class AuthResponse(decrypt: ByteArray) : LoginServerBasePacket(decrypt) {
    val serverId: Int = readC()
    val serverName: String = readS()

}