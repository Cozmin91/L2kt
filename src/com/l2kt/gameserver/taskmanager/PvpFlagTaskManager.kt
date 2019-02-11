package com.l2kt.gameserver.taskmanager

import java.util.concurrent.ConcurrentHashMap

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.model.actor.instance.Player

/**
 * Updates and clears PvP flag of [Player] after specified time.
 */
object PvpFlagTaskManager : Runnable {
    private val players = ConcurrentHashMap<Player, Long>()

    init {
        ThreadPool.scheduleAtFixedRate(this, 1000, 1000)
    }

    override fun run() {
        if (players.isEmpty())
            return

        val currentTime = System.currentTimeMillis()

        for ((player, timeLeft) in players) {
            when {
                currentTime > timeLeft -> {
                    player.updatePvPFlag(0)
                    players.remove(player)
                }
                currentTime > timeLeft - 5000 -> player.updatePvPFlag(2)
                else -> player.updatePvPFlag(1)
            }
        }
    }

    fun add(player: Player, time: Long) {
        players[player] = System.currentTimeMillis() + time
    }

    fun remove(player: Player) {
        players.remove(player)
    }
}