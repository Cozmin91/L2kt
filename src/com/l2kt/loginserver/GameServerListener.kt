package com.l2kt.loginserver

import com.l2kt.Config
import java.io.IOException
import java.net.Socket
import java.util.*

class GameServerListener @Throws(IOException::class)
constructor() : FloodProtectedListener(Config.GAME_SERVER_LOGIN_HOST, Config.GAME_SERVER_LOGIN_PORT) {

    override fun addClient(s: Socket) {
        gameservers.add(GameServerThread(s))
    }

    fun removeGameServer(gst: GameServerThread) {
        gameservers.remove(gst)
    }

    companion object {
        private val gameservers = ArrayList<GameServerThread>()
    }
}