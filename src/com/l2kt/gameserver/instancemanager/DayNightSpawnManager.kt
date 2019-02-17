package com.l2kt.gameserver.instancemanager

import com.l2kt.gameserver.model.L2Spawn
import com.l2kt.gameserver.model.actor.instance.RaidBoss
import com.l2kt.gameserver.taskmanager.GameTimeTaskManager
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author godson
 */
object DayNightSpawnManager {

    private val _dayCreatures: MutableList<L2Spawn>
    private val _nightCreatures: MutableList<L2Spawn>
    private val _bosses: MutableMap<L2Spawn, RaidBoss>
    private val _log = Logger.getLogger(DayNightSpawnManager::class.java.name)

    /**
     * Manage Spawn/Respawn.
     * @param unSpawnCreatures List with L2Npc must be unspawned
     * @param spawnCreatures List with L2Npc must be spawned
     * @param UnspawnLogInfo String for log info for unspawned L2Npc
     * @param SpawnLogInfo String for log info for spawned L2Npc
     */
    private fun spawnCreatures(
        unSpawnCreatures: List<L2Spawn>,
        spawnCreatures: List<L2Spawn>,
        UnspawnLogInfo: String,
        SpawnLogInfo: String
    ) {
        try {
            if (!unSpawnCreatures.isEmpty()) {
                var i = 0
                for (spawn in unSpawnCreatures) {
                    spawn.setRespawnState(false)
                    val last = spawn.npc
                    if (last != null) {
                        last.deleteMe()
                        i++
                    }
                }
                _log.info("DayNightSpawnManager: Removed $i $UnspawnLogInfo creatures")
            }

            var i = 0
            for (spawnDat in spawnCreatures) {
                spawnDat.setRespawnState(true)
                spawnDat.doSpawn(false)
                i++
            }

            _log.info("DayNightSpawnManager: Spawned $i $SpawnLogInfo creatures")
        } catch (e: Exception) {
            _log.log(Level.WARNING, "Error while spawning creatures: " + e.message, e)
        }

    }

    private fun handleHellmans(boss: RaidBoss, mode: Int) {
        when (mode) {
            0 -> {
                boss.deleteMe()
                _log.info("DayNightSpawnManager: Deleting Hellman raidboss")
            }

            1 -> {
                boss.spawnMe()
                _log.info("DayNightSpawnManager: Spawning Hellman raidboss")
            }
        }
    }

    init {
        _dayCreatures = ArrayList()
        _nightCreatures = ArrayList()
        _bosses = HashMap()

        notifyChangeMode()
    }

    fun addDayCreature(spawnDat: L2Spawn) {
        _dayCreatures.add(spawnDat)
    }

    fun addNightCreature(spawnDat: L2Spawn) {
        _nightCreatures.add(spawnDat)
    }

    /**
     * Spawn Day Creatures, and Unspawn Night Creatures
     */
    fun spawnDayCreatures() {
        spawnCreatures(_nightCreatures, _dayCreatures, "night", "day")
    }

    /**
     * Spawn Night Creatures, and Unspawn Day Creatures
     */
    fun spawnNightCreatures() {
        spawnCreatures(_dayCreatures, _nightCreatures, "day", "night")
    }

    private fun changeMode(mode: Int) {
        if (_nightCreatures.isEmpty() && _dayCreatures.isEmpty())
            return

        when (mode) {
            0 -> {
                spawnDayCreatures()
                specialNightBoss(0)
            }

            1 -> {
                spawnNightCreatures()
                specialNightBoss(1)
            }

            else -> _log.warning("DayNightSpawnManager: Wrong mode sent")
        }
    }

    fun notifyChangeMode() {
        try {
            if (GameTimeTaskManager.isNight)
                changeMode(1)
            else
                changeMode(0)
        } catch (e: Exception) {
            _log.log(Level.WARNING, "Error while notifyChangeMode(): " + e.message, e)
        }

    }

    fun cleanUp() {
        _nightCreatures.clear()
        _dayCreatures.clear()
        _bosses.clear()
    }

    private fun specialNightBoss(mode: Int) {
        try {
            for (infoEntry in _bosses.entries) {
                var boss: RaidBoss? = infoEntry.value
                if (boss == null) {
                    if (mode == 1) {
                        val spawn = infoEntry.key

                        boss = spawn.doSpawn(false) as RaidBoss
                        RaidBossSpawnManager.notifySpawnNightBoss(boss)

                        _bosses[spawn] = boss
                    }
                    continue
                }

                if (boss.npcId == 25328 && boss.raidStatus == RaidBossSpawnManager.StatusEnum.ALIVE)
                    handleHellmans(boss, mode)

                return
            }
        } catch (e: Exception) {
            _log.log(Level.WARNING, "Error while specialNoghtBoss(): " + e.message, e)
        }

    }

    fun handleBoss(spawnDat: L2Spawn): RaidBoss? {
        if (_bosses.containsKey(spawnDat))
            return _bosses[spawnDat]

        if (GameTimeTaskManager.isNight) {
            val raidboss = spawnDat.doSpawn(false) as RaidBoss
            _bosses[spawnDat] = raidboss

            return raidboss
        }
        //_bosses[spawnDat] = null

        return null
    }
}