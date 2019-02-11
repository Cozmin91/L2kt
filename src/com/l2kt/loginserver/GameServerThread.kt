package com.l2kt.loginserver

import com.l2kt.Config
import com.l2kt.commons.logging.CLogger
import com.l2kt.loginserver.crypt.NewCrypt
import com.l2kt.loginserver.model.GameServerInfo
import com.l2kt.loginserver.network.gameserverpackets.*
import com.l2kt.loginserver.network.loginserverpackets.*
import com.l2kt.loginserver.network.serverpackets.ServerBasePacket
import java.io.BufferedOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.*

class GameServerThread(private val _connection: Socket) : Thread() {

    private val _accountsOnGameServer = HashSet<String>()
    private val _connectionIp: String = _connection.inetAddress.hostAddress

    private val _publicKey: RSAPublicKey
    private val _privateKey: RSAPrivateKey

    private lateinit var _in: InputStream
    private lateinit var _out: OutputStream

    private var _blowfish: NewCrypt
    private lateinit var _blowfishKey: ByteArray

    private var gameServerInfo: GameServerInfo? = null

    var connectionIpAddress: String? = null
        private set

    val playerCount: Int
        get() = _accountsOnGameServer.size

    val isAuthed: Boolean
        get() = gameServerInfo != null && gameServerInfo!!.isAuthed

    private val serverId: Int
        get() = if (gameServerInfo == null) -1 else gameServerInfo!!.id

    override fun run() {
        connectionIpAddress = _connection.inetAddress.hostAddress

        // Ensure no further processing for this connection if server is considered as banned.
        if (GameServerThread.isBannedGameserverIP(connectionIpAddress)) {
            LOGGER.info("Banned gameserver with IP {} tried to register.", connectionIpAddress!!)
            forceClose(LoginServerFail.REASON_IP_BANNED)
            return
        }

        try {
            sendPacket(InitLS(_publicKey.modulus.toByteArray()))

            var lengthHi: Int
            var lengthLo: Int
            var length: Int
            var checksumOk: Boolean
            while (true) {
                lengthLo = _in.read()
                lengthHi = _in.read()
                length = lengthHi * 256 + lengthLo

                if (lengthHi < 0 || _connection.isClosed)
                    break

                var data = ByteArray(length - 2)

                var receivedBytes = 0
                var newBytes = 0
                while (newBytes != -1 && receivedBytes < length - 2) {
                    newBytes = _in.read(data, 0, length - 2)
                    receivedBytes += newBytes
                }

                if (receivedBytes != length - 2) {
                    LOGGER.warn("Incomplete packet is sent to the server, closing connection.")
                    break
                }

                // decrypt if we have a key
                data = _blowfish.decrypt(data)
                checksumOk = NewCrypt.verifyChecksum(data)
                if (!checksumOk) {
                    LOGGER.warn("Incorrect packet checksum, closing connection.")
                    return
                }

                val packetType = data[0].toInt() and 0xff
                when (packetType) {
                    0 -> onReceiveBlowfishKey(data)
                    1 -> onGameServerAuth(data)
                    2 -> onReceivePlayerInGame(data)
                    3 -> onReceivePlayerLogOut(data)
                    4 -> onReceiveChangeAccessLevel(data)
                    5 -> onReceivePlayerAuthRequest(data)
                    6 -> onReceiveServerStatus(data)
                    else -> {
                        LOGGER.warn(
                            "Unknown opcode ({}) from gameserver, closing connection.",
                            Integer.toHexString(packetType).toUpperCase()
                        )
                        forceClose(LoginServerFail.NOT_AUTHED)
                    }
                }
            }
        } catch (e: IOException) {
            LOGGER.debug("Couldn't process packet.", e)
        } finally {
            if (isAuthed) {
                gameServerInfo!!.setDown()
                LOGGER.info(
                    "GameServer [{}] {} is now set as disconnected.",
                    serverId,
                    GameServerManager.serverNames[serverId]!!
                )
            }
            LoginServer.gameServerListener.removeGameServer(this)
            LoginServer.gameServerListener.removeFloodProtection(_connectionIp)
        }
    }

    private fun onReceiveBlowfishKey(data: ByteArray) {
        val bfk = BlowFishKey(data, _privateKey)

        _blowfishKey = bfk.key
        _blowfish = NewCrypt(_blowfishKey)
    }

    private fun onGameServerAuth(data: ByteArray) {
        handleRegProcess(GameServerAuth(data))

        if (isAuthed)
            sendPacket(AuthResponse(gameServerInfo!!.id))
    }

    private fun onReceivePlayerInGame(data: ByteArray) {
        if (isAuthed) {
            val pig = PlayerInGame(data)

            for (account in pig.accounts)
                _accountsOnGameServer.add(account)
        } else
            forceClose(LoginServerFail.NOT_AUTHED)
    }

    private fun onReceivePlayerLogOut(data: ByteArray) {
        if (isAuthed) {
            val plo = PlayerLogout(data)

            _accountsOnGameServer.remove(plo.account)
        } else
            forceClose(LoginServerFail.NOT_AUTHED)
    }

    private fun onReceiveChangeAccessLevel(data: ByteArray) {
        if (isAuthed) {
            val cal = ChangeAccessLevel(data)

            LoginController.setAccountAccessLevel(cal.account, cal.level)
            LOGGER.info("Changed {} access level to {}.", cal.account, cal.level)
        } else
            forceClose(LoginServerFail.NOT_AUTHED)
    }

    private fun onReceivePlayerAuthRequest(data: ByteArray) {
        if (isAuthed) {
            val par = PlayerAuthRequest(data)
            val key = LoginController.getKeyForAccount(par.account)

            if (key != null && key.equals(par.key)) {
                LoginController.removeAuthedLoginClient(par.account)
                sendPacket(PlayerAuthResponse(par.account, true))
            } else
                sendPacket(PlayerAuthResponse(par.account, false))
        } else
            forceClose(LoginServerFail.NOT_AUTHED)
    }

    private fun onReceiveServerStatus(data: ByteArray) {
        if (isAuthed)
            ServerStatus(data, serverId) // will do the actions by itself
        else
            forceClose(LoginServerFail.NOT_AUTHED)
    }

    private fun handleRegProcess(gameServerAuth: GameServerAuth) {
        val id = gameServerAuth.desiredID
        val hexId = gameServerAuth.hexID

        var gsi: GameServerInfo? = GameServerManager.registeredGameServers[id]
        // is there a gameserver registered with this id?
        if (gsi != null) {
            // does the hex id match?
            if (Arrays.equals(gsi.hexId, hexId)) {
                // check to see if this GS is already connected
                synchronized(gsi) {
                    if (gsi!!.isAuthed)
                        forceClose(LoginServerFail.REASON_ALREADY_LOGGED_IN)
                    else
                        attachGameServerInfo(gsi!!, gameServerAuth)
                }
            } else {
                // there is already a server registered with the desired id and different hex id
                // try to register this one with an alternative id
                if (Config.ACCEPT_NEW_GAMESERVER && gameServerAuth.acceptAlternateID()) {
                    gsi = GameServerInfo(id, hexId, this)
                    if (GameServerManager.registerWithFirstAvailableId(gsi)) {
                        attachGameServerInfo(gsi, gameServerAuth)
                        GameServerManager.registerServerOnDB(gsi)
                    } else
                        forceClose(LoginServerFail.REASON_NO_FREE_ID)
                } else
                    forceClose(LoginServerFail.REASON_WRONG_HEXID)// server id is already taken, and we cant get a new one for you
            }
        } else {
            // can we register on this id?
            if (Config.ACCEPT_NEW_GAMESERVER) {
                gsi = GameServerInfo(id, hexId, this)
                if (GameServerManager.register(id, gsi)) {
                    attachGameServerInfo(gsi, gameServerAuth)
                    GameServerManager.registerServerOnDB(gsi)
                } else
                    forceClose(LoginServerFail.REASON_ID_RESERVED)// some one took this ID meanwhile
            } else
                forceClose(LoginServerFail.REASON_WRONG_HEXID)
        }
    }

    /**
     * Attachs a GameServerInfo to this Thread
     *  * Updates the GameServerInfo values based on GameServerAuth packet
     *  * **Sets the GameServerInfo as Authed**
     * @param gsi The GameServerInfo to be attached.
     * @param gameServerAuth The server info.
     */
    private fun attachGameServerInfo(gsi: GameServerInfo, gameServerAuth: GameServerAuth) {
        gameServerInfo = gsi
        gsi.gameServerThread = this
        gsi.port = gameServerAuth.port

        if (gameServerAuth.hostName != "*") {
            try {
                gameServerInfo!!.hostName = InetAddress.getByName(gameServerAuth.hostName).hostAddress
            } catch (e: UnknownHostException) {
                LOGGER.error("Couldn't resolve hostname '{}'.", e, gameServerAuth.hostName!!)
                gameServerInfo!!.hostName = _connectionIp
            }

        } else
            gameServerInfo!!.hostName = _connectionIp

        gsi.maxPlayers = gameServerAuth.maxPlayers
        gsi.isAuthed = true

        LOGGER.info(
            "Hooked [{}] {} gameserver on: {}.",
            serverId,
            GameServerManager.serverNames[serverId]!!,
            gameServerInfo!!.hostName!!
        )
    }

    private fun forceClose(reason: Int) {
        sendPacket(LoginServerFail(reason))

        try {
            _connection.close()
        } catch (e: IOException) {
            LOGGER.debug("Failed disconnecting banned server, server is already disconnected.", e)
        }

    }

    private fun sendPacket(sl: ServerBasePacket) {
        try {
            var data = sl.content
            NewCrypt.appendChecksum(data)
            data = _blowfish.crypt(data)

            val len = data.size + 2
            synchronized(_out) {
                _out.write(len and 0xff)
                _out.write(len shr 8 and 0xff)
                _out.write(data)
                _out.flush()
            }
        } catch (e: IOException) {
            LOGGER.error("Exception while sending packet {}.", sl.javaClass.simpleName)
        }

    }

    init {
        try {
            _in = _connection.getInputStream()
            _out = BufferedOutputStream(_connection.getOutputStream())
        } catch (e: IOException) {
            LOGGER.debug("Couldn't process gameserver input stream.", e)
        }

        val pair = GameServerManager.keyPair
        _privateKey = pair.private as RSAPrivateKey
        _publicKey = pair.public as RSAPublicKey

        _blowfish = NewCrypt("_;v.]05-31!|+-%xT!^[$\u0000")
        start()
    }

    fun hasAccountOnGameServer(account: String): Boolean {
        return _accountsOnGameServer.contains(account)
    }

    fun kickPlayer(account: String) {
        sendPacket(KickPlayer(account))
    }

    companion object {
        private val LOGGER = CLogger(GameServerThread::class.java.name)

        private fun isBannedGameserverIP(ipAddress: String?): Boolean {
            return try {
                val netAddress = InetAddress.getByName(ipAddress)
                LoginController.isBannedAddress(netAddress)
            } catch (e: UnknownHostException) {
                LOGGER.debug("Failed retrieving gameserver ip.", e)
                true
            }
        }
    }
}