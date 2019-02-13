package com.l2kt.loginserver

import com.l2kt.Config
import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.lang.StringUtil.printSection
import com.l2kt.commons.logging.CLogger
import com.l2kt.commons.mmocore.SelectorConfig
import com.l2kt.commons.mmocore.SelectorThread
import com.l2kt.loginserver.network.LoginClient
import com.l2kt.loginserver.network.LoginPacketHandler
import java.io.*
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.logging.LogManager

object LoginServer {

    lateinit var gameServerListener: GameServerListener
    private lateinit var selectorThread: SelectorThread<LoginClient>
    private val LOGGER = CLogger(LoginServer::class.java.name)

    const val PROTOCOL_REV = 0x0102

    @JvmStatic
    fun main(args: Array<String>) {
        LoginServer
    }

    init {
        File("./log").mkdir()
        File("./log/console").mkdir()
        File("./log/error").mkdir()

        FileInputStream(File("config/logging.properties")).use {
            LogManager.getLogManager().readConfiguration(it)
        }

        printSection("L2kt")

        Config.loadLoginServer()

        L2DatabaseFactory

        printSection("LoginController")
        LoginController

        printSection("GameServerManager")
        GameServerManager

        printSection("Ban List")
        loadBanFile()

        printSection("IP, Ports & Socket infos")
        var bindAddress: InetAddress? = null
        if (Config.LOGIN_BIND_ADDRESS != "*") {
            try {
                bindAddress = InetAddress.getByName(Config.LOGIN_BIND_ADDRESS)
            } catch (uhe: UnknownHostException) {
                LOGGER.error("The LoginServer bind address is invalid, using all available IPs.", uhe)
            }

        }

        val sc = SelectorConfig()
        sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS
        sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS
        sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME
        sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT

        val lph = LoginPacketHandler()
        val sh = SelectorHelper()
        try {
            selectorThread = SelectorThread(sc, sh, lph, sh, sh)
        } catch (ioe: IOException) {
            LOGGER.error("Failed to open selector.", ioe)

            System.exit(1)
        }

        try {
            gameServerListener = GameServerListener()
            gameServerListener.start()

            LOGGER.info(
                "Listening for gameservers on {}:{}.",
                Config.GAME_SERVER_LOGIN_HOST,
                Config.GAME_SERVER_LOGIN_PORT
            )
        } catch (ioe: IOException) {
            LOGGER.error("Failed to start the gameserver listener.", ioe)

            System.exit(1)
        }

        try {
            selectorThread.openServerSocket(bindAddress, Config.PORT_LOGIN)
        } catch (ioe: IOException) {
            LOGGER.error("Failed to open server socket.", ioe)

            System.exit(1)
        }

        selectorThread.start()
        LOGGER.info(
            "Loginserver ready on {}:{}.",
            if (bindAddress == null) "*" else bindAddress.hostAddress,
            Config.PORT_LOGIN
        )

        printSection("Waiting for gameserver answer")
    }

    fun shutdown(restart: Boolean) {
        Runtime.getRuntime().exit(if (restart) 2 else 0)
    }

    private fun loadBanFile() {
        val banFile = File("config/banned_ips.properties")
        if (!banFile.exists() || !banFile.isFile){
            LOGGER.warn("banned_ips.properties is missing. Ban listing is skipped.")
            return
        }

        try {
            LineNumberReader(FileReader(banFile)).use { reader ->
                reader.forEachLine {
                    var line = it
                    var parts: Array<String>
                    line = line.trim{ it <= ' ' }
                    if (line.isNotEmpty() && line[0] != '#') {
                        parts = line.split("#".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                        line = parts[0]
                        parts = line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                        val address = parts[0]
                        var duration: Long = 0

                        if (parts.size > 1) {
                            try {
                                duration = parts[1].toLong()
                            } catch (e: NumberFormatException) {
                                LOGGER.error(
                                    "Incorrect ban duration ({}). Line: {}.",
                                    parts[1],
                                    reader.lineNumber
                                )
                                return@forEachLine
                            }

                        }

                        try {
                            LoginController.addBanForAddress(address, duration)
                        } catch (e: UnknownHostException) {
                            LOGGER.error("Invalid address ({}). Line: {}.", parts[0], reader.lineNumber)
                        }
                    }
                }
            }
        } catch (e: IOException) {
            LOGGER.error("Error while reading banned_ips.properties.", e)
        }

        LOGGER.info("Loaded {} banned IP(s).", LoginController.bannedIps.size)
    }
}