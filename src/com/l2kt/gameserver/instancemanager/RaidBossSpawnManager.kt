package com.l2kt.gameserver.instancemanager

import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.SpawnTable
import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.model.L2Spawn
import com.l2kt.gameserver.model.actor.instance.RaidBoss
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.templates.StatsSet
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ScheduledFuture
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author godson
 */
object RaidBossSpawnManager {

    val _log = Logger.getLogger(RaidBossSpawnManager::class.java.name)

    val _bosses: MutableMap<Int, RaidBoss> = HashMap()
    val _spawns: MutableMap<Int, L2Spawn> = HashMap()
    val _storedInfo: MutableMap<Int, StatsSet> = HashMap()
    val _schedules: MutableMap<Int, ScheduledFuture<*>> = HashMap()

    val bosses: Map<Int, RaidBoss>
        get() = _bosses

    val spawns: Map<Int, L2Spawn>
        get() = _spawns

    enum class StatusEnum {
        ALIVE,
        DEAD,
        UNDEFINED
    }

    init {
        init()
    }

    private fun init() {
        try {
            L2DatabaseFactory.connection.use { con ->
                val statement = con.prepareStatement("SELECT * from raidboss_spawnlist ORDER BY boss_id")
                val rset = statement.executeQuery()

                while (rset.next()) {
                    val template = getValidTemplate(rset.getInt("boss_id"))
                    if (template != null) {
                        val spawnDat = L2Spawn(template)
                        spawnDat.setLoc(
                            rset.getInt("loc_x"),
                            rset.getInt("loc_y"),
                            rset.getInt("loc_z"),
                            rset.getInt("heading")
                        )
                        spawnDat.respawnMinDelay = rset.getInt("spawn_time")
                        spawnDat.respawnMaxDelay = rset.getInt("random_time")

                        addNewSpawn(
                            spawnDat,
                            rset.getLong("respawn_time"),
                            rset.getDouble("currentHP"),
                            rset.getDouble("currentMP"),
                            false
                        )
                    } else {
                        _log.warning("RaidBossSpawnManager: Could not load raidboss #" + rset.getInt("boss_id") + " from DB")
                    }
                }

                _log.info("RaidBossSpawnManager: Loaded " + _bosses.size + " instances.")
                _log.info("RaidBossSpawnManager: Scheduled " + _schedules.size + " instances.")

                rset.close()
                statement.close()
            }
        } catch (e: SQLException) {
            _log.warning("RaidBossSpawnManager: Couldnt load raidboss_spawnlist table.")
        } catch (e: Exception) {
            _log.log(Level.WARNING, "Error while initializing RaidBossSpawnManager: " + e.message, e)
        }

    }

    private class spawnSchedule(private val bossId: Int) : Runnable {

        override fun run() {
            var raidboss: RaidBoss? = null

            if (bossId == 25328)
                raidboss = DayNightSpawnManager.handleBoss(_spawns[bossId] ?: return)
            else
                raidboss = _spawns[bossId]?.doSpawn(false) as RaidBoss?

            if (raidboss != null) {
                raidboss.raidStatus = StatusEnum.ALIVE

                val info = StatsSet()
                info["currentHP"] = raidboss.currentHp
                info["currentMP"] = raidboss.currentMp
                info["respawnTime"] = 0.0

                _storedInfo[bossId] = info

                _log.info("RaidBoss: " + raidboss.name + " has spawned.")

                _bosses[bossId] = raidboss
            }

            _schedules.remove(bossId)
        }
    }

    fun updateStatus(boss: RaidBoss, isBossDead: Boolean) {
        if (!_storedInfo.containsKey(boss.npcId))
            return

        val info = _storedInfo[boss.npcId] ?: StatsSet()

        if (isBossDead) {
            boss.raidStatus = StatusEnum.DEAD

            // getRespawnMinDelay() is used as fixed timer, while getRespawnMaxDelay() is used as random timer.
            val respawnDelay = boss.spawn!!.respawnMinDelay + Rnd[-boss.spawn!!.respawnMaxDelay, boss.spawn!!.respawnMaxDelay]
            val respawnTime = Calendar.getInstance().timeInMillis + respawnDelay * 3600000

            info["currentHP"] = boss.maxHp.toDouble()
            info["currentMP"] = boss.maxMp.toDouble()
            info["respawnTime"] = respawnTime.toDouble()

            if (!_schedules.containsKey(boss.npcId)) {
                _log.info("RaidBoss: " + boss.name + " - " + SimpleDateFormat("dd-MM-yyyy HH:mm").format(respawnTime) + " (" + respawnDelay + "h).")

                _schedules[boss.npcId] = ThreadPool.schedule(spawnSchedule(boss.npcId), (respawnDelay * 3600000).toLong())!!
                updateDb()
            }
        } else {
            boss.raidStatus = StatusEnum.ALIVE

            info["currentHP"] = boss.currentHp
            info["currentMP"] = boss.currentMp
            info["respawnTime"] = 0.0
        }

        _storedInfo[boss.npcId] = info
    }

    fun addNewSpawn(spawnDat: L2Spawn?, respawnTime: Long, currentHP: Double, currentMP: Double, storeInDb: Boolean) {
        var currentHP = currentHP
        var currentMP = currentMP
        if (spawnDat == null)
            return

        val bossId = spawnDat.npcId
        if (_spawns.containsKey(bossId))
            return

        val time = Calendar.getInstance().timeInMillis

        SpawnTable.addNewSpawn(spawnDat, false)

        if (respawnTime == 0L || time > respawnTime) {
            var raidboss: RaidBoss? = null

            if (bossId == 25328)
                raidboss = DayNightSpawnManager.handleBoss(spawnDat)
            else
                raidboss = spawnDat.doSpawn(false) as RaidBoss?

            if (raidboss != null) {
                currentHP = if (currentHP == 0.0) raidboss.maxHp.toDouble() else currentHP
                currentMP = if (currentMP == 0.0) raidboss.maxMp.toDouble() else currentMP

                raidboss.currentHp = currentHP
                raidboss.currentMp = currentMP
                raidboss.raidStatus = StatusEnum.ALIVE

                _bosses[bossId] = raidboss

                val info = StatsSet()
                info["currentHP"] = currentHP
                info["currentMP"] = currentMP
                info["respawnTime"] = 0.0

                _storedInfo[bossId] = info
            }
        } else {
            val spawnTime = respawnTime - Calendar.getInstance().timeInMillis
            _schedules[bossId] = ThreadPool.schedule(spawnSchedule(bossId), spawnTime)!!
        }

        _spawns[bossId] = spawnDat

        if (storeInDb) {
            try {
                L2DatabaseFactory.connection.use { con ->
                    val statement =
                        con.prepareStatement("INSERT INTO raidboss_spawnlist (boss_id,loc_x,loc_y,loc_z,heading,respawn_time,currentHp,currentMp) values(?,?,?,?,?,?,?,?)")
                    statement.setInt(1, spawnDat.npcId)
                    statement.setInt(2, spawnDat.locX)
                    statement.setInt(3, spawnDat.locY)
                    statement.setInt(4, spawnDat.locZ)
                    statement.setInt(5, spawnDat.heading)
                    statement.setLong(6, respawnTime)
                    statement.setDouble(7, currentHP)
                    statement.setDouble(8, currentMP)
                    statement.execute()
                    statement.close()
                }
            } catch (e: Exception) {
                // problem with storing spawn
                _log.log(
                    Level.WARNING,
                    "RaidBossSpawnManager: Could not store raidboss #" + bossId + " in the DB:" + e.message,
                    e
                )
            }

        }
    }

    fun deleteSpawn(spawnDat: L2Spawn?, updateDb: Boolean) {
        if (spawnDat == null)
            return

        val bossId = spawnDat.npcId
        if (!_spawns.containsKey(bossId))
            return

        SpawnTable.deleteSpawn(spawnDat, false)
        _spawns.remove(bossId)

        if (_bosses.containsKey(bossId))
            _bosses.remove(bossId)

        if (_schedules.containsKey(bossId)) {
            val f = _schedules.remove(bossId)
            f?.cancel(true)
        }

        if (_storedInfo.containsKey(bossId))
            _storedInfo.remove(bossId)

        if (updateDb) {
            try {
                L2DatabaseFactory.connection.use { con ->
                    val statement = con.prepareStatement("DELETE FROM raidboss_spawnlist WHERE boss_id=?")
                    statement.setInt(1, bossId)
                    statement.execute()
                    statement.close()
                }
            } catch (e: Exception) {
                // problem with deleting spawn
                _log.log(
                    Level.WARNING,
                    "RaidBossSpawnManager: Could not remove raidboss #" + bossId + " from DB: " + e.message,
                    e
                )
            }

        }
    }

    private fun updateDb() {
        try {
            L2DatabaseFactory.connection.use { con ->
                val statement =
                    con.prepareStatement("UPDATE raidboss_spawnlist SET respawn_time = ?, currentHP = ?, currentMP = ? WHERE boss_id = ?")

                for ((bossId, value) in _storedInfo) {

                    val boss = _bosses[bossId] ?: continue

                    if (boss.raidStatus == StatusEnum.ALIVE)
                        updateStatus(boss, false)

                    val info = value ?: continue

                    statement.setLong(1, info.getLong("respawnTime"))
                    statement.setDouble(2, info.getDouble("currentHP"))
                    statement.setDouble(3, info.getDouble("currentMP"))
                    statement.setInt(4, bossId)
                    statement.executeUpdate()
                    statement.clearParameters()
                }
                statement.close()
            }
        } catch (e: SQLException) {
            _log.log(Level.WARNING, "RaidBossSpawnManager: Couldnt update raidboss_spawnlist table " + e.message, e)
        }

    }

    fun getRaidBossStatusId(bossId: Int): StatusEnum {
        if (_bosses.containsKey(bossId))
            return _bosses[bossId]?.raidStatus ?: StatusEnum.UNDEFINED

        return if (_schedules.containsKey(bossId)) StatusEnum.DEAD else StatusEnum.UNDEFINED

    }

    fun getValidTemplate(bossId: Int): NpcTemplate? {
        val template = NpcData.getTemplate(bossId) ?: return null

        return if (!template.isType("RaidBoss")) null else template

    }

    fun notifySpawnNightBoss(raidboss: RaidBoss) {
        val info = StatsSet()
        info["currentHP"] = raidboss.currentHp
        info["currentMP"] = raidboss.currentMp
        info["respawnTime"] = 0.0

        raidboss.raidStatus = StatusEnum.ALIVE

        _storedInfo[raidboss.npcId] = info
        _bosses[raidboss.npcId] = raidboss

        _log.info("RaidBossSpawnManager: Spawning Night Raid Boss " + raidboss.name)
    }

    fun isDefined(bossId: Int): Boolean {
        return _spawns.containsKey(bossId)
    }

    fun reloadBosses() {
        init()
    }

    /**
     * Saves all raidboss status and then clears all info from memory, including all schedules.
     */
    fun cleanUp() {
        updateDb()

        _bosses.clear()

        if (!_schedules.isEmpty()) {
            for (f in _schedules.values)
                f.cancel(true)

            _schedules.clear()
        }

        _storedInfo.clear()
        _spawns.clear()
    }
}