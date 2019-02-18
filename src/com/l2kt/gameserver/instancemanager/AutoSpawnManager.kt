package com.l2kt.gameserver.instancemanager

import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.SpawnTable
import com.l2kt.gameserver.data.xml.MapRegionData
import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.extensions.announceToOnlinePlayers
import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.model.L2Spawn
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.location.SpawnLocation
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Allows spawning of a NPC object based on a timer (from the official idea used for the Merchant and Blacksmith of Mammon).
 * <P>
 * General Usage:<BR></BR>
 * Call registerSpawn() with the parameters listed below:
</P> * <UL>
 * <LI>int npcId int[][] spawnPoints or specify NULL to add points later.</LI>
 * <LI>int initialDelay (If < 0 = default value)</LI>
 * <LI>int respawnDelay (If < 0 = default value)</LI>
 * <LI>int despawnDelay (If < 0 = default value or if = 0, function disabled)</LI>
</UL> *
 * spawnPoints is a standard two-dimensional int array containing X,Y and Z coordinates. The default respawn/despawn delays are currently every hour (as for Mammon on official servers).<BR></BR>
 * <LI>The resulting AutoSpawnInstance object represents the newly added spawn index. - The interal methods of this object can be used to adjust random spawning, for instance a call to setRandomSpawn(1, true); would set the spawn at index 1 to be randomly rather than sequentially-based. - Also they
 * can be used to specify the number of NPC instances to spawn using setSpawnCount(), and broadcast a message to all users using setBroadcast(). Random Spawning = OFF by default Broadcasting = OFF by default
 * @author Tempy
</LI> */
object AutoSpawnManager {

    var _registeredSpawns: MutableMap<Int, AutoSpawnInstance> = ConcurrentHashMap()
    var _runningSpawns: MutableMap<Int, ScheduledFuture<*>> = ConcurrentHashMap()
    val _log = Logger.getLogger(AutoSpawnManager::class.java.name)

    private const val DEFAULT_INITIAL_SPAWN = 30000 // 30 seconds after registration
    private const val DEFAULT_RESPAWN = 3600000 // 1 hour in millisecs
    private const val DEFAULT_DESPAWN = 3600000 // 1 hour in millisecs
    var _activeState = true

    init {
        restoreSpawnData()
    }

    fun size(): Int {
        return _registeredSpawns.size
    }

    private fun restoreSpawnData() {
        try {
            L2DatabaseFactory.connection.use { con ->
                // Restore spawn group data, then the location data.
                val statement = con.prepareStatement("SELECT * FROM random_spawn ORDER BY groupId ASC")
                val rs = statement.executeQuery()

                while (rs.next()) {
                    // Register random spawn group, set various options on the
                    // created spawn instance.
                    val spawnInst = registerSpawn(
                        rs.getInt("npcId"),
                        rs.getInt("initialDelay"),
                        rs.getInt("respawnDelay"),
                        rs.getInt("despawnDelay")
                    )

                    spawnInst.spawnCount = rs.getInt("count")
                    spawnInst.setBroadcast(rs.getBoolean("broadcastSpawn"))
                    spawnInst.isRandomSpawn = rs.getBoolean("randomSpawn")
                    // Restore the spawn locations for this spawn group/instance.
                    val statement2 = con.prepareStatement("SELECT * FROM random_spawn_loc WHERE groupId=?")
                    statement2.setInt(1, rs.getInt("groupId"))
                    val rs2 = statement2.executeQuery()

                    while (rs2.next()) {
                        // Add each location to the spawn group/instance.
                        spawnInst.addSpawnLocation(
                            rs2.getInt("x"),
                            rs2.getInt("y"),
                            rs2.getInt("z"),
                            rs2.getInt("heading")
                        )
                    }
                    rs2.close()
                    statement2.close()
                }
                rs.close()
                statement.close()
            }
        } catch (e: Exception) {
            _log.warning("AutoSpawnManager: Could not restore spawn data: $e")
        }

    }

    /**
     * Registers a spawn with the given parameters with the spawner, and marks it as active.<br></br>
     * Returns a AutoSpawnInstance containing info about the spawn.
     * @param npcId
     * @param spawnPoints
     * @param initialDelay : (If < 0 = default value)
     * @param respawnDelay : (If < 0 = default value)
     * @param despawnDelay : despawnDelay (If < 0 = default value or if = 0, function disabled)
     * @return AutoSpawnInstance spawnInst
     */
    fun registerSpawn(
        npcId: Int,
        spawnPoints: Array<IntArray>?,
        initialDelay: Int,
        respawnDelay: Int,
        despawnDelay: Int
    ): AutoSpawnInstance {
        var initialDelay = initialDelay
        var respawnDelay = respawnDelay
        var despawnDelay = despawnDelay
        if (initialDelay < 0)
            initialDelay = DEFAULT_INITIAL_SPAWN

        if (respawnDelay < 0)
            respawnDelay = DEFAULT_RESPAWN

        if (despawnDelay < 0)
            despawnDelay = DEFAULT_DESPAWN

        val newSpawn = AutoSpawnInstance(npcId, initialDelay, respawnDelay, despawnDelay)

        if (spawnPoints != null)
            for (spawnPoint in spawnPoints)
                newSpawn.addSpawnLocation(spawnPoint)

        val newId = IdFactory.getInstance().nextId
        newSpawn.objectId = newId
        _registeredSpawns[newId] = newSpawn

        setSpawnActive(newSpawn, true)

        return newSpawn
    }

    /**
     * Registers a spawn with the given parameters with the spawner, and marks it as active.<BR></BR>
     * Returns a AutoSpawnInstance containing info about the spawn.<BR></BR>
     * <BR></BR>
     * <B>Warning:</B> Spawn locations must be specified separately using addSpawnLocation().
     * @param npcId
     * @param initialDelay (If < 0 = default value)
     * @param respawnDelay (If < 0 = default value)
     * @param despawnDelay (If < 0 = default value or if = 0, function disabled)
     * @return AutoSpawnInstance spawnInst
     */
    fun registerSpawn(npcId: Int, initialDelay: Int, respawnDelay: Int, despawnDelay: Int): AutoSpawnInstance {
        return registerSpawn(npcId, null, initialDelay, respawnDelay, despawnDelay)
    }

    /**
     * Remove a registered spawn from the list, specified by the given spawn instance.
     * @param spawnInst
     * @return boolean removedSuccessfully
     */
    fun removeSpawn(spawnInst: AutoSpawnInstance): Boolean {
        if (!isSpawnRegistered(spawnInst))
            return false

        try {
            // Try to remove from the list of registered spawns if it exists.
            _registeredSpawns.remove(spawnInst.objectId)

            // Cancel the currently associated running scheduled task.
            val respawnTask = _runningSpawns.remove(spawnInst.objectId)
            respawnTask?.cancel(false)
        } catch (e: Exception) {
            _log.warning("AutoSpawnManager: Could not auto spawn for NPC ID " + spawnInst.npcId + " (Object ID = " + spawnInst.objectId + "): " + e)
            return false
        }

        return true
    }

    /**
     * Sets the active state of the specified spawn.
     * @param spawnInst
     * @param isActive
     */
    fun setSpawnActive(spawnInst: AutoSpawnInstance?, isActive: Boolean) {
        if (spawnInst == null)
            return

        val objectId = spawnInst.objectId

        if (isSpawnRegistered(objectId)) {
            var spawnTask: ScheduledFuture<*>

            if (isActive) {
                val rs = AutoSpawner(objectId)

                if (spawnInst.despawnDelay > 0)
                    spawnTask = ThreadPool.scheduleAtFixedRate(
                        rs,
                        spawnInst.initialDelay.toLong(),
                        spawnInst.respawnDelay.toLong()
                    )!!
                else
                    spawnTask = ThreadPool.schedule(rs, spawnInst.initialDelay.toLong())!!

                _runningSpawns[objectId] = spawnTask
            } else {
                val rd = AutoDespawner(objectId)
                spawnTask = _runningSpawns.remove(objectId)!!

                spawnTask.cancel(false)

                ThreadPool.schedule(rd, 0)
            }

            spawnInst.isSpawnActive = isActive
        }
    }

    /**
     * Sets the active state of all auto spawn instances to that specified, and cancels the scheduled spawn task if necessary.
     * @param isActive
     */
    fun setAllActive(isActive: Boolean) {
        if (_activeState == isActive)
            return

        for (spawnInst in _registeredSpawns.values)
            setSpawnActive(spawnInst, isActive)

        _activeState = isActive
    }

    /**
     * Returns the number of milliseconds until the next occurrance of the given spawn.
     * @param spawnInst
     * @return
     */
    fun getTimeToNextSpawn(spawnInst: AutoSpawnInstance): Long {
        val objectId = spawnInst.objectId

        return if (!isSpawnRegistered(objectId)) -1 else _runningSpawns[objectId]?.getDelay(TimeUnit.MILLISECONDS) ?: -1

    }

    /**
     * Attempts to return the AutoSpawnInstance associated with the given NPC or Object ID type. <BR></BR>
     * Note: If isObjectId == false, returns first instance for the specified NPC ID.
     * @param id
     * @param isObjectId
     * @return AutoSpawnInstance spawnInst
     */
    fun getAutoSpawnInstance(id: Int, isObjectId: Boolean): AutoSpawnInstance? {
        if (isObjectId) {
            if (isSpawnRegistered(id))
                return _registeredSpawns[id]
        } else {
            for (spawnInst in _registeredSpawns.values)
                if (spawnInst.npcId == id)
                    return spawnInst
        }
        return null
    }

    fun getAutoSpawnInstances(npcId: Int): Map<Int, AutoSpawnInstance> {
        val spawnInstList = HashMap<Int, AutoSpawnInstance>()

        for (spawnInst in _registeredSpawns.values)
            if (spawnInst.npcId == npcId)
                spawnInstList[spawnInst.objectId] = spawnInst

        return spawnInstList
    }

    /**
     * Tests if the specified object ID is assigned to an auto spawn.
     * @param objectId
     * @return boolean isAssigned
     */
    fun isSpawnRegistered(objectId: Int): Boolean {
        return _registeredSpawns.containsKey(objectId)
    }

    /**
     * Tests if the specified spawn instance is assigned to an auto spawn.
     * @param spawnInst
     * @return boolean isAssigned
     */
    fun isSpawnRegistered(spawnInst: AutoSpawnInstance): Boolean {
        return _registeredSpawns.containsValue(spawnInst)
    }

    /**
     * AutoSpawner Class This handles the main spawn task for an auto spawn instance, and initializes a despawner if required.
     * @author Tempy
     */
    private class AutoSpawner(private val _objectId: Int) : Runnable {

        override fun run() {
            try {
                // Retrieve the required spawn instance for this spawn task.
                val spawnInst = _registeredSpawns[_objectId] ?: return

                // If the spawn is not scheduled to be active, cancel the spawn task.
                if (!spawnInst.isSpawnActive)
                    return

                val locationList = spawnInst.locationList

                // If there are no set co-ordinates, cancel the spawn task.
                if (locationList.isEmpty()) {
                    _log.info("AutoSpawnManager: No location co-ords specified for spawn instance (Object ID = $_objectId).")
                    return
                }

                val locationCount = locationList.size
                var locationIndex = Rnd[locationCount]

                // If random spawning is disabled, the spawn at the next set of co-ordinates after the last. If the index is greater than the number of possible spawns, reset the counter to zero.
                if (!spawnInst.isRandomSpawn) {
                    locationIndex = spawnInst._lastLocIndex
                    locationIndex++

                    if (locationIndex == locationCount)
                        locationIndex = 0

                    spawnInst._lastLocIndex = locationIndex
                }

                // Set the X, Y and Z co-ordinates, where this spawn will take place.
                val x = locationList[locationIndex].x
                val y = locationList[locationIndex].y
                val z = locationList[locationIndex].z
                val heading = locationList[locationIndex].heading

                // Fetch the template for this NPC ID and create a new spawn.
                val npcTemp = NpcData.getTemplate(spawnInst.npcId)
                if (npcTemp == null) {
                    _log.warning("Couldnt find npcId: " + spawnInst.npcId + ".")
                    return
                }
                val newSpawn = L2Spawn(npcTemp)
                newSpawn.setLoc(x, y, z, heading)

                if (spawnInst.despawnDelay == 0)
                    newSpawn.respawnDelay = spawnInst.respawnDelay

                // Add the new spawn information to the spawn table, but do not store it.
                SpawnTable.addNewSpawn(newSpawn, false)
                var npcInst: Npc? = null

                if (spawnInst.spawnCount == 1) {
                    npcInst = newSpawn.doSpawn(false)
                    npcInst!!.setXYZ(npcInst.x, npcInst.y, npcInst.z)
                    spawnInst.addNpcInstance(npcInst)
                } else {
                    for (i in 0 until spawnInst.spawnCount) {
                        npcInst = newSpawn.doSpawn(false)

                        // To prevent spawning of more than one NPC in the exact same spot, move it slightly by a small random offset.
                        npcInst!!.setXYZ(npcInst.x + Rnd[50], npcInst.y + Rnd[50], npcInst.z)

                        // Add the NPC instance to the list of managed instances.
                        spawnInst.addNpcInstance(npcInst)
                    }
                }

                // Announce to all players that the spawn has taken place, with the nearest town location.
                if (npcInst != null && spawnInst.isBroadcasting)
                    ("The " + npcInst.name + " has spawned near " + MapRegionData.getClosestTownName(
                        npcInst.x,
                        npcInst.y
                    ) + "!").announceToOnlinePlayers()

                // If there is no despawn time, do not create a despawn task.
                if (spawnInst.despawnDelay > 0)
                    ThreadPool.schedule(AutoDespawner(_objectId), (spawnInst.despawnDelay - 1000).toLong())
            } catch (e: Exception) {
                _log.log(
                    Level.WARNING,
                    "AutoSpawnManager: An error occurred while initializing spawn instance (Object ID = " + _objectId + "): " + e.message,
                    e
                )
            }

        }
    }

    /**
     * AutoDespawner Class <BR></BR>
     * <BR></BR>
     * Simply used as a secondary class for despawning an auto spawn instance.
     * @author Tempy
     */
    private class AutoDespawner(private val _objectId: Int) : Runnable {

        override fun run() {
            try {
                val spawnInst = _registeredSpawns[_objectId]

                if (spawnInst == null) {
                    _log.info("AutoSpawnManager: No spawn registered for object ID = $_objectId.")
                    return
                }

                for (npcInst in spawnInst.npcInstanceList) {

                    SpawnTable.deleteSpawn(npcInst.spawn!!, false)
                    npcInst.deleteMe()
                    spawnInst.removeNpcInstance(npcInst)
                }
            } catch (e: Exception) {
                _log.warning("AutoSpawnManager: An error occurred while despawning spawn (Object ID = $_objectId): $e")
            }

        }
    }

    /**
     * AutoSpawnInstance Class <BR></BR>
     * <BR></BR>
     * Stores information about a registered auto spawn.
     * @author Tempy
     */
    class AutoSpawnInstance(var npcId: Int, var initialDelay: Int, var respawnDelay: Int, var despawnDelay: Int) {
        var objectId: Int = 0

        var spawnCount = 1

        var _lastLocIndex = -1

        private val _npcList = ArrayList<Npc>()

        private val _locList = ArrayList<SpawnLocation>()

        var isSpawnActive: Boolean = false

        var isRandomSpawn = false

        var isBroadcasting = false
            private set

        val locationList: Array<SpawnLocation>
            get() = _locList.toTypedArray()

        val npcInstanceList: Array<Npc>
            get() {
                var ret: Array<Npc?>
                synchronized(_npcList) {
                    ret = arrayOfNulls(_npcList.size)
                    _npcList.toArray(ret)
                    return ret.filterNotNull().toTypedArray()
                }
            }

        val spawns: Array<L2Spawn>
            get() {
                val npcSpawns = ArrayList<L2Spawn>()

                for (npcInst in _npcList)
                    npcSpawns.add(npcInst.spawn!!)

                return npcSpawns.toTypedArray()
            }

        fun addNpcInstance(npcInst: Npc): Boolean {
            return _npcList.add(npcInst)
        }

        fun removeNpcInstance(npcInst: Npc): Boolean {
            return _npcList.remove(npcInst)
        }

        fun setBroadcast(broadcastValue: Boolean) {
            isBroadcasting = broadcastValue
        }

        fun addSpawnLocation(x: Int, y: Int, z: Int, heading: Int): Boolean {
            return _locList.add(SpawnLocation(x, y, z, heading))
        }

        fun addSpawnLocation(spawnLoc: IntArray): Boolean {
            return if (spawnLoc.size != 3) false else addSpawnLocation(spawnLoc[0], spawnLoc[1], spawnLoc[2], -1)

        }
    }
}