package com.l2kt.loginserver.network.loginserverpackets

import com.l2kt.loginserver.GameServerManager
import com.l2kt.loginserver.network.serverpackets.ServerBasePacket

class AuthResponse(serverId: Int) : ServerBasePacket() {

    override val content: ByteArray
        get() = bytes

    init {
        writeC(0x02)
        writeC(serverId)
        writeS(GameServerManager.serverNames[serverId])
    }
}