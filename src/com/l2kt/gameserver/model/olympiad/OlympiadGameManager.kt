package com.l2kt.gameserver.model.olympiad

import com.l2kt.gameserver.data.manager.ZoneManager
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.zone.type.OlympiadStadiumZone
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author GodKratos, DS
 */
object OlympiadGameManager : Runnable {
    private val _log = Logger.getLogger(OlympiadGameManager::class.java.name)

    @Volatile
    var isBattleStarted = false
        private set
    val olympiadTasks: Array<OlympiadGameTask?>

    val isAllTasksFinished: Boolean
        get() {
            for (task in olympiadTasks) {
                if (task?.isRunning == true)
                    return false
            }
            return true
        }

    val numberOfStadiums: Int
        get() = olympiadTasks.size

    init {
        val zones = ZoneManager.getAllZones(OlympiadStadiumZone::class.java)
        if (zones == null || zones.isEmpty())
            throw Error("No olympiad stadium zones defined !")

        olympiadTasks = arrayOfNulls(zones.size)
        var i = 0
        for (zone in zones)
            olympiadTasks[i++] = OlympiadGameTask(zone)

        _log.log(Level.INFO, "Olympiad: Loaded " + olympiadTasks.size + " stadiums.")
    }

    fun startBattle() {
        isBattleStarted = true
    }

    override fun run() {
        if (Olympiad.isOlympiadEnd)
            return

        if (Olympiad.inCompPeriod()) {
            var task: OlympiadGameTask
            var newGame: AbstractOlympiadGame?

            var readyClassed = OlympiadManager.hasEnoughRegisteredClassed()
            var readyNonClassed = OlympiadManager.hasEnoughRegisteredNonClassed()

            if (readyClassed != null || readyNonClassed) {
                // set up the games queue
                for (i in olympiadTasks.indices){
                    task = olympiadTasks[i]!!
                    synchronized(task) {
                        if (!task.isRunning) {
                            // Fair arena distribution
                            // 0,2,4,6,8.. arenas checked for classed or teams first
                            if (readyClassed != null && i % 2 == 0) {
                                // if no ready teams found check for classed
                                newGame = OlympiadGameClassed.createGame(i, readyClassed)
                                if (newGame != null) {
                                    task.attachGame(newGame)
                                    return@synchronized
                                }
                                readyClassed = null
                            }
                            // 1,3,5,7,9.. arenas used for non-classed
                            // also other arenas will be used for non-classed if no classed or teams available
                            if (readyNonClassed) {
                                newGame = OlympiadGameNonClassed.createGame(i, OlympiadManager.registeredNonClassBased)
                                if (newGame != null) {
                                    task.attachGame(newGame)
                                    return@synchronized
                                }
                                readyNonClassed = false
                            }
                        }
                    }

                    // stop generating games if no more participants
                    if (readyClassed == null && !readyNonClassed)
                        break
                }
            }
        } else {
            // not in competition period
            if (isAllTasksFinished) {
                OlympiadManager.clearRegistered()
                isBattleStarted = false
                _log.log(Level.INFO, "Olympiad: All current games finished.")
            }
        }
    }

    fun getOlympiadTask(id: Int): OlympiadGameTask? {
        return if (id < 0 || id >= olympiadTasks.size) null else olympiadTasks[id]

    }

    fun notifyCompetitorDamage(player: Player?, damage: Int) {
        if (player == null)
            return

        val id = player.olympiadGameId
        if (id < 0 || id >= olympiadTasks.size)
            return

        val game = olympiadTasks[id]?.game
        game?.addDamage(player, damage)
    }
}