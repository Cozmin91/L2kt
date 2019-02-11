package com.l2kt.gameserver.data

import com.l2kt.Config
import com.l2kt.L2DatabaseFactory
import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.instancemanager.DayNightSpawnManager
import com.l2kt.gameserver.model.L2Spawn
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

/**
 * @author Nightmare
 */
object SpawnTable {

    private val _spawntable = ConcurrentHashMap.newKeySet<L2Spawn>()
    private val _log = Logger.getLogger(SpawnTable::class.java.name)

    val spawnTable: Set<L2Spawn>
        get() = _spawntable

    init {
        if (!Config.ALT_DEV_NO_SPAWNS)
            fillSpawnTable()
    }

    private fun fillSpawnTable() {
        try {
            L2DatabaseFactory.connection.use { con ->
                val statement = con.prepareStatement("SELECT * FROM spawnlist")
                val rset = statement.executeQuery()

                var spawnDat: L2Spawn

                while (rset.next()) {
                    val template1 = NpcData.getInstance().getTemplate(rset.getInt("npc_templateid"))
                    if (template1 != null) {
                        if (template1.isType("SiegeGuard")) {
                            // Don't spawn guards, they're spawned during castle sieges.
                        } else if (template1.isType("RaidBoss")) {
                            // Don't spawn raidbosses ; raidbosses are supposed to be loaded in another table !
                            _log.warning("SpawnTable: RB (" + template1.idTemplate + ") is in regular spawnlist, move it in raidboss_spawnlist.")
                        } else if (!Config.ALLOW_CLASS_MASTERS && template1.isType("ClassMaster")) {
                            // Dont' spawn class masters (if config is setuped to false).
                        } else if (!Config.WYVERN_ALLOW_UPGRADER && template1.isType("WyvernManagerNpc")) {
                            // Dont' spawn wyvern managers (if config is setuped to false).
                        } else {
                            spawnDat = L2Spawn(template1)
                            spawnDat.setLoc(
                                rset.getInt("locx"),
                                rset.getInt("locy"),
                                rset.getInt("locz"),
                                rset.getInt("heading")
                            )
                            spawnDat.respawnDelay = rset.getInt("respawn_delay")
                            spawnDat.respawnRandom = rset.getInt("respawn_rand")

                            when (rset.getInt("periodOfDay")) {
                                0 // default
                                -> {
                                    spawnDat.setRespawnState(true)
                                    spawnDat.doSpawn(false)
                                }

                                1 // Day
                                -> DayNightSpawnManager.getInstance().addDayCreature(spawnDat)

                                2 // Night
                                -> DayNightSpawnManager.getInstance().addNightCreature(spawnDat)
                            }

                            _spawntable.add(spawnDat)
                        }
                    } else {
                        _log.warning("SpawnTable: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".")
                    }
                }
                rset.close()
                statement.close()
            }
        } catch (e: Exception) {
            // problem with initializing spawn, go to next one
            _log.warning("SpawnTable: Spawn could not be initialized: $e")
        }

        _log.config("SpawnTable: Loaded " + _spawntable.size + " Npc Spawn Locations.")
    }

    fun addNewSpawn(spawn: L2Spawn, storeInDb: Boolean) {
        _spawntable.add(spawn)

        if (storeInDb) {
            try {
                L2DatabaseFactory.connection.use { con ->
                    val statement =
                        con.prepareStatement("INSERT INTO spawnlist (npc_templateid,locx,locy,locz,heading,respawn_delay) values(?,?,?,?,?,?)")
                    statement.setInt(1, spawn.npcId)
                    statement.setInt(2, spawn.locX)
                    statement.setInt(3, spawn.locY)
                    statement.setInt(4, spawn.locZ)
                    statement.setInt(5, spawn.heading)
                    statement.setInt(6, spawn.respawnDelay)
                    statement.execute()
                    statement.close()
                }
            } catch (e: Exception) {
                // problem with storing spawn
                _log.warning("SpawnTable: Could not store spawn in the DB:$e")
            }

        }
    }

    fun deleteSpawn(spawn: L2Spawn, updateDb: Boolean) {
        if (!_spawntable.remove(spawn))
            return

        if (updateDb) {
            try {
                L2DatabaseFactory.connection.use { con ->
                    val statement =
                        con.prepareStatement("DELETE FROM spawnlist WHERE locx=? AND locy=? AND locz=? AND npc_templateid=? AND heading=?")
                    statement.setInt(1, spawn.locX)
                    statement.setInt(2, spawn.locY)
                    statement.setInt(3, spawn.locZ)
                    statement.setInt(4, spawn.npcId)
                    statement.setInt(5, spawn.heading)
                    statement.execute()
                    statement.close()
                }
            } catch (e: Exception) {
                // problem with deleting spawn
                _log.warning("SpawnTable: Spawn $spawn could not be removed from DB: $e")
            }

        }
    }

    // just wrapper
    fun reloadAll() {
        _spawntable.clear()
        fillSpawnTable()
    }
}