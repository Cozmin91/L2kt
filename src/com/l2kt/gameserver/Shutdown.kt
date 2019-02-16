package com.l2kt.gameserver

import com.l2kt.Config
import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.lang.StringUtil
import com.l2kt.commons.logging.CLogger
import com.l2kt.gameserver.data.manager.*
import com.l2kt.gameserver.data.sql.ServerMemoTable
import com.l2kt.gameserver.extensions.announceToOnlinePlayers
import com.l2kt.gameserver.extensions.toAllOnlinePlayers
import com.l2kt.gameserver.instancemanager.GrandBossManager
import com.l2kt.gameserver.instancemanager.RaidBossSpawnManager
import com.l2kt.gameserver.instancemanager.SevenSigns
import com.l2kt.gameserver.instancemanager.SevenSignsFestival
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.entity.Hero
import com.l2kt.gameserver.model.olympiad.Olympiad
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.gameserverpackets.ServerStatus
import com.l2kt.gameserver.network.serverpackets.ServerClose
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.taskmanager.ItemsOnGroundTaskManager
import kotlin.reflect.jvm.jvmName

class Shutdown : Thread {

    private var _secondsShut: Int = 0
    private var _shutdownMode: Int = 0

    private constructor() {
        _secondsShut = -1
        _shutdownMode = SIGTERM
    }

    constructor(seconds: Int, restart: Boolean) {
        _secondsShut = Math.max(0, seconds)
        _shutdownMode = if (restart) GM_RESTART else GM_SHUTDOWN
    }

    /**
     * This function is called, when a new thread starts if this thread is the thread of getInstance, then this is the shutdown hook and we save all data and disconnect all clients.<br></br>
     * <br></br>
     * After this thread ends, the server will completely exit if this is not the thread of getInstance, then this is a countdown thread.<br></br>
     * <br></br>
     * We start the countdown, and when we finished it, and it was not aborted, we tell the shutdown-hook why we call exit, and then call exit when the exit status of the server is 1, startServer.sh / startServer.bat will restart the server.
     */
    override fun run() {
        if (this === SingletonHolder.INSTANCE) {
            StringUtil.printSection("Under " + MODE_TEXT[_shutdownMode] + " process")

            try {
                disconnectAllPlayers()
                LOGGER.info("All players have been disconnected.")
            } catch (t: Throwable) {
            }

            ThreadPool.shutdown()

            try {
                LoginServerThread.interrupt()
            } catch (t: Throwable) {
            }

            if (!SevenSigns.getInstance().isSealValidationPeriod)
                SevenSignsFestival.getInstance().saveFestivalData(false)

            SevenSigns.getInstance().saveSevenSignsData()
            SevenSigns.getInstance().saveSevenSignsStatus()
            LOGGER.info("Seven Signs Festival, general data && status have been saved.")

            ZoneManager.save()

            RaidBossSpawnManager.getInstance().cleanUp()
            LOGGER.info("Raid Bosses data has been saved.")

            GrandBossManager.getInstance().cleanUp()
            LOGGER.info("World Bosses data has been saved.")

            Olympiad.saveOlympiadStatus()
            LOGGER.info("Olympiad data has been saved.")

            Hero.getInstance().shutdown()
            LOGGER.info("Hero data has been saved.")

            CastleManorManager.storeMe()
            LOGGER.info("Manors data has been saved.")

            FishingChampionshipManager.shutdown()
            LOGGER.info("Fishing Championship data has been saved.")

            BufferManager.saveSchemes()
            LOGGER.info("BufferTable data has been saved.")

            if (Config.ALLOW_WEDDING) {
                CoupleManager.save()
                LOGGER.info("CoupleManager data has been saved.")
            }

            ServerMemoTable.storeMe()
            LOGGER.info("ServerMemo data has been saved.")

            ItemsOnGroundTaskManager.save()

            try {
                Thread.sleep(5000)
            } catch (e: InterruptedException) {
            }

            try {
                GameServer.selectorThread.shutdown()
            } catch (t: Throwable) {
            }

            try {
                L2DatabaseFactory.shutdown()
            } catch (t: Throwable) {
            }

            Runtime.getRuntime().halt(if (SingletonHolder.INSTANCE._shutdownMode == GM_RESTART) 2 else 0)
        } else {
            countdown()

            when (_shutdownMode) {
                GM_SHUTDOWN -> {
                    SingletonHolder.INSTANCE.setShutdownMode(GM_SHUTDOWN)
                    SingletonHolder.INSTANCE.run()
                    System.exit(0)
                }

                GM_RESTART -> {
                    SingletonHolder.INSTANCE.setShutdownMode(GM_RESTART)
                    SingletonHolder.INSTANCE.run()
                    System.exit(2)
                }
            }
        }
    }

    /**
     * This functions starts a shutdown countdown.
     * @param player : The [Player] who issued the shutdown command.
     * @param ghostEntity : The entity who issued the shutdown command.
     * @param seconds : The number of seconds until shutdown.
     * @param restart : If true, the server will restart after shutdown.
     */
    fun startShutdown(player: Player?, ghostEntity: String, seconds: Int, restart: Boolean) {
        _shutdownMode = if (restart) GM_RESTART else GM_SHUTDOWN

        if (player != null)
            LOGGER.info("GM: {} issued {} process in {} seconds.", player.toString(), MODE_TEXT[_shutdownMode], seconds)
        else if (!ghostEntity.isEmpty())
            LOGGER.info("Entity: {} issued {} process in {} seconds.", ghostEntity, MODE_TEXT[_shutdownMode], seconds)

        if (_shutdownMode > 0) {
            when (seconds) {
                540, 480, 420, 360, 300, 240, 180, 120, 60, 30, 10, 5, 4, 3, 2, 1 -> {
                }
                else -> sendServerQuit(seconds)
            }
        }

        if (counterInstance != null)
            counterInstance!!.setShutdownMode(ABORT)

        counterInstance = Shutdown(seconds, restart)
        counterInstance!!.start()
    }

    /**
     * This function aborts a running countdown.
     * @param player : The [Player] who issued the abort process.
     */
    fun abort(player: Player) {
        if (counterInstance != null) {
            LOGGER.info("GM: {} aborted {} process.", player.toString(), MODE_TEXT[_shutdownMode])
            counterInstance!!.setShutdownMode(ABORT)

            "Server aborted " + MODE_TEXT[_shutdownMode] + " process and continues normal operation.".announceToOnlinePlayers()
        }
    }

    private fun setShutdownMode(mode: Int) {
        _shutdownMode = mode
    }

    /**
     * Report the current countdown to all players. Flag the server as "down" when reaching 60sec. Rehabilitate the server status if ABORT [ServerStatus] is seen.
     */
    private fun countdown() {
        try {
            while (_secondsShut > 0) {
                if (_shutdownMode == ABORT) {
                    if (LoginServerThread.serverStatus == ServerStatus.STATUS_DOWN)
                        LoginServerThread.serverStatus =
                                if (Config.SERVER_GMONLY) ServerStatus.STATUS_GM_ONLY else ServerStatus.STATUS_AUTO

                    break
                }

                when (_secondsShut) {
                    540, 480, 420, 360, 300, 240, 180, 120, 60, 30, 10, 5, 4, 3, 2, 1 -> sendServerQuit(_secondsShut)
                }

                if (_secondsShut <= 60 && LoginServerThread.serverStatus != ServerStatus.STATUS_DOWN) {
                    LoginServerThread.serverStatus = ServerStatus.STATUS_DOWN
                }

                _secondsShut--

                Thread.sleep(1000)
            }
        } catch (e: InterruptedException) {
        }

    }

    private object SingletonHolder {
        internal val INSTANCE = Shutdown()
    }

    companion object {
        private val LOGGER = CLogger(Shutdown::class.jvmName)

        private var counterInstance: Shutdown? = null

        const val SIGTERM = 0
        const val GM_SHUTDOWN = 1
        const val GM_RESTART = 2
        const val ABORT = 3
        private val MODE_TEXT = arrayOf("SIGTERM", "shutting down", "restarting", "aborting")

        private fun sendServerQuit(seconds: Int) {
            SystemMessage.getSystemMessage(SystemMessageId.THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECONDS).addNumber(seconds).toAllOnlinePlayers()
        }

        private fun disconnectAllPlayers() {
            for (player in World.players) {
                val client = player.client
                if (client != null && !client.isDetached) {
                    client.close(ServerClose.STATIC_PACKET)
                    client.activeChar = null

                    player.client = null
                }
                player.deleteMe()
            }
        }

        val instance: Shutdown
            get() = SingletonHolder.INSTANCE
    }
}