package com.l2kt.gameserver

import com.l2kt.Config
import com.l2kt.commons.logging.CLogger
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.network.L2GameClient
import com.l2kt.gameserver.network.L2GameClient.GameClientState
import com.l2kt.gameserver.network.gameserverpackets.*
import com.l2kt.gameserver.network.loginserverpackets.*
import com.l2kt.gameserver.network.serverpackets.AuthLoginFail
import com.l2kt.gameserver.network.serverpackets.AuthLoginFail.FailReason
import com.l2kt.gameserver.network.serverpackets.CharSelectInfo
import com.l2kt.loginserver.crypt.NewCrypt
import java.io.BufferedOutputStream
import java.io.IOException
import java.io.OutputStream
import java.math.BigInteger
import java.net.Socket
import java.net.UnknownHostException
import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAKeyGenParameterSpec
import java.security.spec.RSAPublicKeySpec
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object LoginServerThread : Thread("LoginServerThread") {

    private val clients = ConcurrentHashMap<String, L2GameClient>()
    val LOGGER = CLogger(LoginServerThread::class.java.name)

    private const val REVISION = 0x0102

    fun generateHex(size: Int): ByteArray {
        val array = ByteArray(size)
        Rnd.nextBytes(array)
        return array
    }

    var serverName: String? = null

    private var loginSocket: Socket? = null
    private lateinit var outputStream: OutputStream

    private var blowfish: NewCrypt? = null
    private var hexId: ByteArray? = null

    private var requestId: Int = 0
    var maxPlayers: Int = 0
        private set
    var serverStatus: Int = 0
        set(status) {
            sendServerStatus(ServerStatus.STATUS, status)
            field = status
        }

    val statusString: String
        get() = ServerStatus.STATUS_STRING[serverStatus]

    init {

        hexId = Config.HEX_ID
        if (hexId == null) {
            requestId = Config.REQUEST_ID
            hexId = generateHex(16)
        } else
            requestId = Config.SERVER_ID

        maxPlayers = Config.MAXIMUM_ONLINE_USERS
    }

    override fun run() {
        while (!isInterrupted) {
            try {
                LOGGER.info(
                    "Connecting to login on {}:{}.",
                    Config.GAME_SERVER_LOGIN_HOST,
                    Config.GAME_SERVER_LOGIN_PORT
                )

                loginSocket = Socket(Config.GAME_SERVER_LOGIN_HOST, Config.GAME_SERVER_LOGIN_PORT)
                val _in = loginSocket!!.getInputStream()
                outputStream = BufferedOutputStream(loginSocket!!.getOutputStream())

                val _blowfishKey = generateHex(40)
                blowfish = NewCrypt("_;v.]05-31!|+-%xT!^[$\u0000")

                while (!isInterrupted) {
                    val lengthLo = _in.read()
                    val lengthHi = _in.read()
                    val length = lengthHi * 256 + lengthLo

                    if (lengthHi < 0)
                        break

                    val incoming = ByteArray(length - 2)

                    var receivedBytes = 0
                    var newBytes = 0
                    var left = length - 2

                    while (newBytes != -1 && receivedBytes < length - 2) {
                        newBytes = _in.read(incoming, receivedBytes, left)
                        receivedBytes += newBytes
                        left -= newBytes
                    }

                    if (receivedBytes != length - 2) {
                        LOGGER.warn("Incomplete packet is sent to the server, closing connection.")
                        break
                    }

                    val decrypt = blowfish!!.decrypt(incoming)

                    if (!NewCrypt.verifyChecksum(decrypt)) {
                        LOGGER.warn("Incorrect packet checksum, ignoring packet.")
                        break
                    }

                    val packetType = decrypt[0].toInt() and 0xff
                    val publicKey: RSAPublicKey
                    when (packetType) {
                        0x00 -> {
                            val init = InitLS(decrypt)

                            if (init.revision != REVISION) {
                                LOGGER.warn("Revision mismatch between LS and GS.")
                                return
                            }

                            try {
                                val kfac = KeyFactory.getInstance("RSA")
                                val modulus = BigInteger(init.rsaKey)
                                val kspec1 = RSAPublicKeySpec(modulus, RSAKeyGenParameterSpec.F4)

                                publicKey = kfac.generatePublic(kspec1) as RSAPublicKey
                            } catch (e: GeneralSecurityException) {
                                LOGGER.error("Troubles while init the public key sent by login.")
                                return
                            }

                            sendPacket(BlowFishKey(_blowfishKey, publicKey))
                            blowfish = NewCrypt(_blowfishKey)

                            sendPacket(
                                AuthRequest(
                                    requestId,
                                    Config.ACCEPT_ALTERNATE_ID,
                                    hexId!!,
                                    Config.HOSTNAME,
                                    Config.PORT_GAME,
                                    Config.RESERVE_HOST_ON_LOGIN,
                                    maxPlayers
                                )
                            )
                        }

                        0x01 -> {
                            val lsf = LoginServerFail(decrypt)
                            LOGGER.info("LoginServer registration failed: {}.", lsf.reasonString)
                        }

                        0x02 -> {
                            val aresp = AuthResponse(decrypt)

                            val serverId = aresp.serverId
                            serverName = aresp.serverName

                            Config.saveHexid(serverId, BigInteger(hexId!!).toString(16))
                            LOGGER.info("Registered as server: [{}] {}.", serverId, serverName!!)

                            val ss = ServerStatus()
                            ss.addAttribute(
                                ServerStatus.STATUS,
                                if (Config.SERVER_GMONLY) ServerStatus.STATUS_GM_ONLY else ServerStatus.STATUS_AUTO
                            )
                            ss.addAttribute(ServerStatus.CLOCK, Config.SERVER_LIST_CLOCK)
                            ss.addAttribute(ServerStatus.BRACKETS, Config.SERVER_LIST_BRACKET)
                            ss.addAttribute(ServerStatus.AGE_LIMIT, Config.SERVER_LIST_AGE)
                            ss.addAttribute(ServerStatus.TEST_SERVER, Config.SERVER_LIST_TESTSERVER)
                            ss.addAttribute(ServerStatus.PVP_SERVER, Config.SERVER_LIST_PVPSERVER)
                            sendPacket(ss)

                            val players = World.getInstance().players
                            if (!players.isEmpty()) {
                                val playerList = ArrayList<String>()
                                for (player in players)
                                    playerList.add(player.accountName)

                                sendPacket(PlayerInGame(playerList))
                            }
                        }

                        0x03 -> {
                            val par = PlayerAuthResponse(decrypt)

                            val client = clients[par.account] ?: return

                            if (par.isAuthed) {
                                sendPacket(PlayerInGame(par.account))

                                client.state = GameClientState.AUTHED
                                client.sendPacket(CharSelectInfo(par.account, client.sessionId!!.playOkID1))
                            } else {
                                client.sendPacket(AuthLoginFail(FailReason.SYSTEM_ERROR_LOGIN_LATER))
                                client.closeNow()
                            }
                        }

                        0x04 -> {
                            val kp = KickPlayer(decrypt)
                            kickPlayer(kp.account)
                        }
                    }
                }
            } catch (e: UnknownHostException) {
            } catch (e: IOException) {
                LOGGER.error("No connection found with loginserver, next try in 10 seconds.")
            } finally {
                try {
                    loginSocket!!.close()
                    if (isInterrupted)
                        return
                } catch (e: Exception) {
                }

            }

            // 10 seconds tempo before another try
            try {
                Thread.sleep(10000)
            } catch (e: InterruptedException) {
                return
            }

        }
    }

    fun sendLogout(account: String?) {
        if (account == null)
            return

        try {
            sendPacket(PlayerLogout(account))
        } catch (e: IOException) {
            LOGGER.error("Error while sending logout packet to login.")
        } finally {
            clients.remove(account)
        }
    }

    fun addClient(account: String, client: L2GameClient) {
        val existingClient = (clients).putIfAbsent(account, client)
        if (existingClient == null) {
                sendPacket(PlayerAuthRequest(client.accountName!!, client.sessionId!!))

        } else {
            client.closeNow()
            existingClient.closeNow()
        }
    }

    fun sendAccessLevel(account: String, level: Int) {
        try {
            sendPacket(ChangeAccessLevel(account, level))
        } catch (e: IOException) {
        }

    }

    fun kickPlayer(account: String) {
        val client = clients[account]
        client?.closeNow()
    }

    @Throws(IOException::class)
    private fun sendPacket(sl: GameServerBasePacket) {
        var data = sl.content
        NewCrypt.appendChecksum(data)

        data = blowfish!!.crypt(data)

        val len = data.size + 2

        synchronized(outputStream) // avoids tow threads writing in the mean time
        {
            outputStream.write(len and 0xff)
            outputStream.write(len shr 8 and 0xff)
            outputStream.write(data)
            outputStream.flush()
        }
    }

    fun setMaxPlayer(maxPlayers: Int) {
        sendServerStatus(ServerStatus.MAX_PLAYERS, maxPlayers)

        this.maxPlayers = maxPlayers
    }

    fun sendServerStatus(id: Int, value: Int) {
        try {
            val ss = ServerStatus()
            ss.addAttribute(id, value)

            sendPacket(ss)
        } catch (e: IOException) {
        }
    }
}