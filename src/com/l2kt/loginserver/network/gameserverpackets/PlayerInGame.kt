package com.l2kt.loginserver.network.gameserverpackets

import com.l2kt.loginserver.network.clientpackets.ClientBasePacket
import java.util.*

class PlayerInGame(decrypt: ByteArray) : ClientBasePacket(decrypt) {
    val accounts = ArrayList<String>()

    init {
        val size = readH()
        for (i in 0 until size)
            accounts.add(readS())
    }
}