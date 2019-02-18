package com.l2kt.gameserver.instancemanager

import com.l2kt.L2DatabaseFactory
import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.model.actor.instance.GrandBoss
import com.l2kt.gameserver.templates.StatsSet
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * This class handles the status of all Grand Bosses, and manages L2BossZone zones.
 * @author DaRkRaGe, Emperorc
 */
object GrandBossManager {

    var _log = Logger.getLogger(GrandBossManager::class.java.name)

    private val SELECT_GRAND_BOSS_DATA = "SELECT * from grandboss_data ORDER BY boss_id"
    private val UPDATE_GRAND_BOSS_DATA =
        "UPDATE grandboss_data set loc_x = ?, loc_y = ?, loc_z = ?, heading = ?, respawn_time = ?, currentHP = ?, currentMP = ?, status = ? where boss_id = ?"
    private val UPDATE_GRAND_BOSS_DATA2 = "UPDATE grandboss_data set status = ? where boss_id = ?"
    private val _bosses = HashMap<Int, GrandBoss>()
    private val _storedInfo = HashMap<Int, StatsSet>()
    private val _bossStatus = HashMap<Int, Int>()

    init {
        try {
            L2DatabaseFactory.connection.use { con ->
                val statement = con.prepareStatement(SELECT_GRAND_BOSS_DATA)
                val rset = statement.executeQuery()

                while (rset.next()) {
                    val info = StatsSet()

                    val bossId = rset.getInt("boss_id")

                    info["loc_x"] = rset.getInt("loc_x").toDouble()
                    info["loc_y"] = rset.getInt("loc_y").toDouble()
                    info["loc_z"] = rset.getInt("loc_z").toDouble()
                    info["heading"] = rset.getInt("heading").toDouble()
                    info["respawn_time"] = rset.getLong("respawn_time").toDouble()
                    info["currentHP"] = rset.getDouble("currentHP")
                    info["currentMP"] = rset.getDouble("currentMP")

                    _bossStatus[bossId] = rset.getInt("status")
                    _storedInfo[bossId] = info
                }
                rset.close()
                statement.close()

                _log.info("GrandBossManager: Loaded " + _storedInfo.size + " GrandBosses instances.")
            }
        } catch (e: Exception) {
            _log.log(Level.WARNING, "GrandBossManager: Could not load grandboss data: " + e.message, e)
        }

    }

    fun getBossStatus(bossId: Int): Int {
        return _bossStatus[bossId] ?: 0
    }

    fun setBossStatus(bossId: Int, status: Int) {
        _bossStatus[bossId] = status
        _log.info("GrandBossManager: Updated " + NpcData.getTemplate(bossId)!!.name + " (id: " + bossId + ") status to " + status)
        updateDb(bossId, true)
    }

    /**
     * Adds a L2GrandBossInstance to the list of bosses.
     * @param boss The boss to add.
     */
    fun addBoss(boss: GrandBoss?) {
        if (boss != null)
            _bosses[boss.npcId] = boss
    }

    /**
     * Adds a L2GrandBossInstance to the list of bosses. Using this variant of addBoss, we can impose a npcId.
     * @param npcId The npcId to use for registration.
     * @param boss The boss to add.
     */
    fun addBoss(npcId: Int, boss: GrandBoss?) {
        if (boss != null)
            _bosses[npcId] = boss
    }

    fun getBoss(bossId: Int): GrandBoss? {
        return _bosses[bossId]
    }

    fun getStatsSet(bossId: Int): StatsSet? {
        return _storedInfo[bossId]
    }

    fun setStatsSet(bossId: Int, info: StatsSet) {
        _storedInfo[bossId] = info
        updateDb(bossId, false)
    }

    private fun storeToDb() {
        try {
            L2DatabaseFactory.connection.use { con ->
                val updateStatement1 = con.prepareStatement(UPDATE_GRAND_BOSS_DATA2)
                val updateStatement2 = con.prepareStatement(UPDATE_GRAND_BOSS_DATA)

                for ((bossId, info) in _storedInfo) {

                    val boss = _bosses[bossId]
                    if (boss == null || info == null) {
                        updateStatement1.setInt(1, _bossStatus[bossId] ?: 0)
                        updateStatement1.setInt(2, bossId)
                        updateStatement1.executeUpdate()
                        updateStatement1.clearParameters()
                    } else {
                        updateStatement2.setInt(1, boss.x)
                        updateStatement2.setInt(2, boss.y)
                        updateStatement2.setInt(3, boss.z)
                        updateStatement2.setInt(4, boss.heading)
                        updateStatement2.setLong(5, info.getLong("respawn_time"))
                        updateStatement2.setDouble(6, if (boss.isDead()) boss.maxHp.toDouble() else boss.currentHp)
                        updateStatement2.setDouble(7, if (boss.isDead()) boss.maxMp.toDouble() else boss.currentMp)
                        updateStatement2.setInt(8, _bossStatus[bossId] ?: 0)
                        updateStatement2.setInt(9, bossId)
                        updateStatement2.executeUpdate()
                        updateStatement2.clearParameters()
                    }
                }
                updateStatement1.close()
                updateStatement2.close()
            }
        } catch (e: SQLException) {
            _log.log(Level.WARNING, "GrandBossManager: Couldn't store grandbosses to database:" + e.message, e)
        }

    }

    private fun updateDb(bossId: Int, statusOnly: Boolean) {
        try {
            L2DatabaseFactory.connection.use { con ->
                val boss = _bosses[bossId]
                val info = _storedInfo[bossId]
                var statement: PreparedStatement? = null

                if (statusOnly || boss == null || info == null) {
                    statement = con.prepareStatement(UPDATE_GRAND_BOSS_DATA2)
                    statement!!.setInt(1, _bossStatus[bossId] ?: 0)
                    statement.setInt(2, bossId)
                } else {
                    statement = con.prepareStatement(UPDATE_GRAND_BOSS_DATA)
                    statement!!.setInt(1, boss.x)
                    statement.setInt(2, boss.y)
                    statement.setInt(3, boss.z)
                    statement.setInt(4, boss.heading)
                    statement.setLong(5, info.getLong("respawn_time"))
                    statement.setDouble(6, if (boss.isDead()) boss.maxHp.toDouble() else boss.currentHp)
                    statement.setDouble(7, if (boss.isDead()) boss.maxMp.toDouble() else boss.currentMp)
                    statement.setInt(8, _bossStatus[bossId] ?: 0)
                    statement.setInt(9, bossId)
                }
                statement.executeUpdate()
                statement.close()
            }
        } catch (e: SQLException) {
            _log.log(Level.WARNING, "GrandBossManager: Couldn't update grandbosses to database:" + e.message, e)
        }

    }

    /**
     * Saves all Grand Boss info and then clears all info from memory, including all schedules.
     */
    fun cleanUp() {
        storeToDb()

        _bosses.clear()
        _storedInfo.clear()
        _bossStatus.clear()
    }
}