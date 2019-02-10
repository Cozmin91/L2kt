package com.l2kt.loginserver.network.gameserverpackets

import com.l2kt.loginserver.GameServerManager
import com.l2kt.loginserver.network.clientpackets.ClientBasePacket
import java.util.logging.Logger

class ServerStatus(decrypt: ByteArray, serverId: Int) : ClientBasePacket(decrypt) {

    init {

        val gsi = GameServerManager.registeredGameServers[serverId]
        if (gsi != null) {
            val size = readD()
            for (i in 0 until size) {
                val type = readD()
                val value = readD()
                when (type) {
                    STATUS -> gsi.status = value
                    CLOCK -> gsi.isShowingClock = value == ON
                    BRACKETS -> gsi.isShowingBrackets = value == ON
                    AGE_LIMIT -> gsi.ageLimit = value
                    TEST_SERVER -> gsi.isTestServer = value == ON
                    PVP_SERVER -> gsi.isPvp = value == ON
                    MAX_PLAYERS -> gsi.maxPlayers = value
                }
            }
        }
    }

    companion object {
        protected var _log = Logger.getLogger(ServerStatus::class.java.name)

        val STATUS_STRING = arrayOf("Auto", "Good", "Normal", "Full", "Down", "Gm Only")

        val STATUS = 0x01
        val CLOCK = 0x02
        val BRACKETS = 0x03
        val AGE_LIMIT = 0x04
        val TEST_SERVER = 0x05
        val PVP_SERVER = 0x06
        val MAX_PLAYERS = 0x07

        val STATUS_AUTO = 0x00
        val STATUS_GOOD = 0x01
        val STATUS_NORMAL = 0x02
        val STATUS_FULL = 0x03
        val STATUS_DOWN = 0x04
        val STATUS_GM_ONLY = 0x05

        val ON = 0x01
        val OFF = 0x00
    }
}