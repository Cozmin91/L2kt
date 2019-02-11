package com.l2kt.gameserver.network.loginserverpackets

class PlayerAuthResponse(decrypt: ByteArray) : LoginServerBasePacket(decrypt) {
    val account: String = readS()
    val isAuthed: Boolean = readC() != 0

}