package com.l2kt.gameserver.model.olympiad

import com.l2kt.Config
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.model.zone.type.OlympiadStadiumZone
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author DS
 */
class OlympiadGameTask(val zone: OlympiadStadiumZone) : Runnable {
    var game: AbstractOlympiadGame? = null
        private set
    private var _state = GameState.IDLE
    private var _needAnnounce = false
    private var _countDown = 0

    val isRunning: Boolean
        get() = _state != GameState.IDLE

    val isGameStarted: Boolean
        get() = _state.ordinal >= GameState.GAME_STARTED.ordinal && _state.ordinal <= GameState.CLEANUP.ordinal

    val isInTimerTime: Boolean
        get() = _state == GameState.BATTLE_COUNTDOWN

    val isBattleStarted: Boolean
        get() = _state == GameState.BATTLE_IN_PROGRESS

    val isBattleFinished: Boolean
        get() = _state == GameState.TELE_TO_TOWN

    private enum class GameState {
        BEGIN,
        TELE_TO_ARENA,
        GAME_STARTED,
        BATTLE_COUNTDOWN,
        BATTLE_STARTED,
        BATTLE_IN_PROGRESS,
        GAME_STOPPED,
        TELE_TO_TOWN,
        CLEANUP,
        IDLE
    }

    init {
        zone.registerTask(this)
    }

    fun needAnnounce(): Boolean {
        if (_needAnnounce) {
            _needAnnounce = false
            return true
        }
        return false
    }

    fun attachGame(game: AbstractOlympiadGame?) {
        if (game != null && _state != GameState.IDLE) {
            _log.log(Level.WARNING, "Attempt to overwrite non-finished game in state $_state")
            return
        }

        this.game = game
        _state = GameState.BEGIN
        _needAnnounce = false
        ThreadPool.execute(this)
    }

    override fun run() {
        try {
            var delay = 1 // schedule next call after 1s
            when (_state) {
                // Game created
                OlympiadGameTask.GameState.BEGIN -> {
                    _state = GameState.TELE_TO_ARENA
                    _countDown = Config.ALT_OLY_WAIT_TIME
                }
                // Teleport to arena countdown
                OlympiadGameTask.GameState.TELE_TO_ARENA -> {
                    game!!.broadcastPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.YOU_WILL_ENTER_THE_OLYMPIAD_STADIUM_IN_S1_SECOND_S).addNumber(
                            _countDown
                        )
                    )

                    delay = getDelay(TELEPORT_TO_ARENA)
                    if (_countDown <= 0)
                        _state = GameState.GAME_STARTED
                }
                // Game start, port players to arena
                OlympiadGameTask.GameState.GAME_STARTED -> run innerRun@{
                    if (!startGame()) {
                        _state = GameState.GAME_STOPPED
                        return@innerRun
                    }

                    _state = GameState.BATTLE_COUNTDOWN
                    _countDown = Config.ALT_OLY_WAIT_BATTLE
                    delay = getDelay(BATTLE_START_TIME)
                }
                // Battle start countdown, first part (60-10)
                OlympiadGameTask.GameState.BATTLE_COUNTDOWN -> {
                    zone.broadcastPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_WILL_START_IN_S1_SECOND_S).addNumber(
                            _countDown
                        )
                    )

                    if (_countDown == 20) {
                        game!!.buffPlayers()
                        game!!.healPlayers()
                    }

                    delay = getDelay(BATTLE_START_TIME)
                    if (_countDown <= 0)
                        _state = GameState.BATTLE_STARTED
                }
                // Beginning of the battle
                OlympiadGameTask.GameState.BATTLE_STARTED -> {
                    _countDown = 0

                    game!!.healPlayers()
                    game!!.resetDamage()

                    _state = GameState.BATTLE_IN_PROGRESS // set state first, used in zone update
                    if (!startBattle())
                        _state = GameState.GAME_STOPPED
                }
                // Checks during battle
                OlympiadGameTask.GameState.BATTLE_IN_PROGRESS -> {
                    _countDown += 1000
                    if (checkBattle() || _countDown > Config.ALT_OLY_BATTLE)
                        _state = GameState.GAME_STOPPED
                }
                // End of the battle
                OlympiadGameTask.GameState.GAME_STOPPED -> {
                    _state = GameState.TELE_TO_TOWN
                    _countDown = Config.ALT_OLY_WAIT_END
                    stopGame()
                    delay = getDelay(TELEPORT_TO_TOWN)
                }
                // Teleport to town countdown
                OlympiadGameTask.GameState.TELE_TO_TOWN -> {
                    game?.broadcastPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.YOU_WILL_BE_MOVED_TO_TOWN_IN_S1_SECONDS).addNumber(
                            _countDown
                        )
                    )

                    delay = getDelay(TELEPORT_TO_TOWN)
                    if (_countDown <= 0)
                        _state = GameState.CLEANUP
                }
                // Removals
                OlympiadGameTask.GameState.CLEANUP -> {
                    cleanupGame()
                    _state = GameState.IDLE
                    game = null
                    return
                }
            }
            ThreadPool.schedule(this, (delay * 1000).toLong())
        } catch (e: Exception) {
            when (_state) {
                OlympiadGameTask.GameState.GAME_STOPPED, OlympiadGameTask.GameState.TELE_TO_TOWN, OlympiadGameTask.GameState.CLEANUP, OlympiadGameTask.GameState.IDLE -> {
                    _log.log(Level.WARNING, "Unable to return players back in town, exception: " + e.message)
                    _state = GameState.IDLE
                    game = null
                    return
                }
            }

            _log.log(Level.WARNING, "Exception in " + _state + ", trying to port players back: " + e.message, e)
            _state = GameState.GAME_STOPPED
            ThreadPool.schedule(this, 1000)
        }

    }

    private fun getDelay(times: IntArray): Int {
        var time: Int
        for (i in 0 until times.size - 1) {
            time = times[i]
            if (time >= _countDown)
                continue

            val delay = _countDown - time
            _countDown = time
            return delay
        }
        // should not happens
        _countDown = -1
        return 1
    }

    /**
     * Second stage: check for defaulted, port players to arena, announce game.
     * @return true if no participants defaulted.
     */
    private fun startGame(): Boolean {
        try {
            // Checking for opponents and teleporting to arena
            if (game!!.checkDefaulted())
                return false

            if (!game!!.portPlayersToArena(zone.locs))
                return false

            game!!.removals()
            _needAnnounce = true
            OlympiadGameManager.startBattle() // inform manager
            return true
        } catch (e: Exception) {
            _log.log(Level.WARNING, e.message, e)
        }

        return false
    }

    /**
     * Fourth stage: last checks, start competition itself.
     * @return true if all participants online and ready on the stadium.
     */
    private fun startBattle(): Boolean {
        try {
            if (game!!.checkBattleStatus() && game!!.makeCompetitionStart()) {
                // game successfully started
                game!!.broadcastOlympiadInfo(zone)
                zone.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.STARTS_THE_GAME))
                zone.updateZoneStatusForCharactersInside()
                return true
            }
        } catch (e: Exception) {
            _log.log(Level.WARNING, e.message, e)
        }

        return false
    }

    /**
     * Fifth stage: battle is running, returns true if winner found.
     * @return
     */
    private fun checkBattle(): Boolean {
        try {
            return game!!.haveWinner()
        } catch (e: Exception) {
            _log.log(Level.WARNING, e.message, e)
        }

        return true
    }

    /**
     * Sixth stage: winner's validations
     */
    private fun stopGame() {
        try {
            game!!.validateWinner(zone)
        } catch (e: Exception) {
            _log.log(Level.WARNING, e.message, e)
        }

        try {
            zone.updateZoneStatusForCharactersInside()
        } catch (e: Exception) {
            _log.log(Level.WARNING, e.message, e)
        }

        try {
            game!!.cleanEffects()
        } catch (e: Exception) {
            _log.log(Level.WARNING, e.message, e)
        }

    }

    /**
     * Seventh stage: game cleanup (port players back, closing doors, etc)
     */
    private fun cleanupGame() {
        try {
            game!!.playersStatusBack()
        } catch (e: Exception) {
            _log.log(Level.WARNING, e.message, e)
        }

        try {
            game!!.portPlayersBack()
        } catch (e: Exception) {
            _log.log(Level.WARNING, e.message, e)
        }

        try {
            game!!.clearPlayers()
        } catch (e: Exception) {
            _log.log(Level.WARNING, e.message, e)
        }

    }

    companion object {
        protected val _log = Logger.getLogger(OlympiadGameTask::class.java.name)
        protected val BATTLE_PERIOD = Config.ALT_OLY_BATTLE // 6 mins

        val TELEPORT_TO_ARENA = intArrayOf(120, 60, 30, 15, 10, 5, 4, 3, 2, 1, 0)
        val BATTLE_START_TIME = intArrayOf(60, 50, 40, 30, 20, 10, 5, 4, 3, 2, 1, 0)
        val TELEPORT_TO_TOWN = intArrayOf(40, 30, 20, 10, 5, 4, 3, 2, 1, 0)
    }
}