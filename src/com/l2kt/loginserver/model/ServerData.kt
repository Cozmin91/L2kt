package com.l2kt.loginserver.model

class ServerData(val status: Int, val hostName: String, gsi: GameServerInfo) {

    val serverId = gsi.id
    val port = gsi.port
    val currentPlayers = gsi.currentPlayerCount
    val maxPlayers = gsi.maxPlayers
    val ageLimit = gsi.ageLimit
    val isPvp = gsi.isPvp
    val isTestServer = gsi.isTestServer
    val isShowingBrackets = gsi.isShowingBrackets
    val isShowingClock = gsi.isShowingClock
}