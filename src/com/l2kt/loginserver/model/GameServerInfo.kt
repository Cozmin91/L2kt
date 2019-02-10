package com.l2kt.loginserver.model

import com.l2kt.loginserver.GameServerThread
import com.l2kt.loginserver.network.gameserverpackets.ServerStatus

data class GameServerInfo @JvmOverloads constructor(
    var id: Int,
    val hexId: ByteArray,
    var gameServerThread: GameServerThread? = null
) {
    var isAuthed: Boolean = false
    var status: Int = ServerStatus.STATUS_DOWN

    var hostName: String? = null
    var port: Int = 0

    var isPvp: Boolean = false
    var isTestServer: Boolean = false
    var isShowingClock: Boolean = false
    var isShowingBrackets: Boolean = false

    var ageLimit: Int = 0
    var maxPlayers: Int = 0

    val currentPlayerCount: Int
        get() = if (gameServerThread == null) 0 else gameServerThread!!.playerCount

    fun setDown() {
        isAuthed = false
        port = 0
        gameServerThread = null
        status = ServerStatus.STATUS_DOWN
    }
}