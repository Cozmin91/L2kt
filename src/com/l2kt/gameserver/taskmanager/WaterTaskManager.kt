package com.l2kt.gameserver.taskmanager

import java.util.concurrent.ConcurrentHashMap

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SetupGauge
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.skills.Stats

/**
 * Updates [Player] drown timer and reduces [Player] HP, when drowning.
 */
object WaterTaskManager : Runnable {
    private val players = ConcurrentHashMap<Player, Long>()

    init {
        ThreadPool.scheduleAtFixedRate(this, 1000, 1000)
    }

    override fun run() {
        if (players.isEmpty())
            return

        val time = System.currentTimeMillis()

        for ((player, value) in players) {
            if (time < value)
                continue

            val hp = player.maxHp / 100.0
            player.reduceCurrentHp(hp, player, false, false, null)
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DROWN_DAMAGE_S1).addNumber(hp.toInt()))
        }
    }

    /**
     * Adds [Player] to the WaterTask.
     * @param player : [Player] to be added and checked.
     */
    fun add(player: Player) {
        if (!player.isDead && !players.containsKey(player)) {
            val time = player.calcStat(Stats.BREATH, 60000 * player.race.breathMultiplier, player, null).toInt()

            players[player] = System.currentTimeMillis() + time

            player.sendPacket(SetupGauge(SetupGauge.GaugeColor.CYAN, time))
        }
    }

    /**
     * Removes [Player] from the WaterTask.
     * @param player : Player to be removed.
     */
    fun remove(player: Player) {
        if (players.remove(player) != null)
            player.sendPacket(SetupGauge(SetupGauge.GaugeColor.CYAN, 0))
    }
}