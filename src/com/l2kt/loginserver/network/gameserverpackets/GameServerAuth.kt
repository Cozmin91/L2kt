package com.l2kt.loginserver.network.gameserverpackets

import com.l2kt.loginserver.network.clientpackets.ClientBasePacket
import java.util.logging.Logger

class GameServerAuth(decrypt: ByteArray) : ClientBasePacket(decrypt) {

    val hexID: ByteArray
    val desiredID: Int
    val hostReserved: Boolean
    private val _acceptAlternativeId: Boolean
    val maxPlayers: Int
    val port: Int
    val hostName: String?

    init {

        desiredID = readC()
        _acceptAlternativeId = if (readC() == 0) false else true
        hostReserved = if (readC() == 0) false else true
        hostName = readS()
        port = readH()
        maxPlayers = readD()
        val size = readD()
        hexID = readB(size)
    }

    fun acceptAlternateID(): Boolean {
        return _acceptAlternativeId
    }

    companion object {
        protected var _log = Logger.getLogger(GameServerAuth::class.java.name)
    }
}