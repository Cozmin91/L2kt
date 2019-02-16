package com.l2kt.gameserver.model

import com.l2kt.commons.logging.CLogger
import com.l2kt.gameserver.data.SpawnTable
import com.l2kt.gameserver.data.sql.PlayerInfoTable
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Pet
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.location.Location
import java.util.concurrent.ConcurrentHashMap

object World {

    private val _objects = ConcurrentHashMap<Int, WorldObject>()
    private val _pets = ConcurrentHashMap<Int, Pet>()
    private val _players = ConcurrentHashMap<Int, Player>()
    private val LOGGER = CLogger(World::class.java.name)

    // Geodata min/max tiles
    const val TILE_X_MIN = 16
    const val TILE_X_MAX = 26
    const val TILE_Y_MIN = 10
    const val TILE_Y_MAX = 25

    // Map dimensions
    const val TILE_SIZE = 32768
    const val WORLD_X_MIN = (TILE_X_MIN - 20) * TILE_SIZE
    const val WORLD_X_MAX = (TILE_X_MAX - 19) * TILE_SIZE
    const val WORLD_Y_MIN = (TILE_Y_MIN - 18) * TILE_SIZE
    const val WORLD_Y_MAX = (TILE_Y_MAX - 17) * TILE_SIZE

    // Regions and offsets
    private const val REGION_SIZE = 2048
    private const val REGIONS_X = (WORLD_X_MAX - WORLD_X_MIN) / REGION_SIZE
    private const val REGIONS_Y = (WORLD_Y_MAX - WORLD_Y_MIN) / REGION_SIZE
    private val REGION_X_OFFSET = Math.abs(WORLD_X_MIN / REGION_SIZE)
    private val REGION_Y_OFFSET = Math.abs(WORLD_Y_MIN / REGION_SIZE)

    fun getRegionX(regionX: Int): Int {
        return (regionX - REGION_X_OFFSET) * REGION_SIZE
    }

    fun getRegionY(regionY: Int): Int {
        return (regionY - REGION_Y_OFFSET) * REGION_SIZE
    }

    /**
     * @param x X position of the object
     * @param y Y position of the object
     * @return True if the given coordinates are valid WorldRegion coordinates.
     */
    private fun validRegion(x: Int, y: Int): Boolean {
        return x in 0..REGIONS_X && y >= 0 && y <= REGIONS_Y
    }

    /**
     * @return the whole 2d array containing the world regions used by ZoneData.java to setup zones inside the world regions
     */
    val worldRegions = Array<Array<WorldRegion?>>(REGIONS_X + 1) { arrayOfNulls(REGIONS_Y + 1) }

    val objects: Collection<WorldObject>
        get() = _objects.values

    val players: Collection<Player>
        get() = _players.values

    init {
        for (i in 0..REGIONS_X) {
            for (j in 0..REGIONS_Y)
                worldRegions[i][j] = WorldRegion(i, j)
        }

        for (x in 0..REGIONS_X) {
            for (y in 0..REGIONS_Y) {
                for (a in -1..1) {
                    for (b in -1..1) {
                        if (validRegion(x + a, y + b))
                            worldRegions[x][y]?.let { worldRegions[x + a][y + b]?.addSurroundingRegion(it) }
                    }
                }
            }
        }
        LOGGER.info("World grid ({} by {}) is now set up.", REGIONS_X, REGIONS_Y)
    }

    fun addObject(`object`: WorldObject) {
        _objects.putIfAbsent(`object`.objectId, `object`)
    }

    fun removeObject(`object`: WorldObject) {
        _objects.remove(`object`.objectId)
    }

    fun getObject(objectId: Int): WorldObject? {
        return _objects[objectId]
    }

    fun addPlayer(cha: Player) {
        _players.putIfAbsent(cha.objectId, cha)
    }

    fun removePlayer(cha: Player) {
        _players.remove(cha.objectId)
    }

    fun getPlayer(name: String): Player? {
        return _players[PlayerInfoTable.getPlayerObjectId(name)]
    }

    fun getPlayer(objectId: Int): Player? {
        return _players[objectId]
    }

    fun addPet(ownerId: Int, pet: Pet) {
        _pets.putIfAbsent(ownerId, pet)
    }

    fun removePet(ownerId: Int) {
        _pets.remove(ownerId)
    }

    fun getPet(ownerId: Int): Pet? {
        return _pets[ownerId]
    }

    /**
     * @param point position of the object.
     * @return the current WorldRegion of the object according to its position (x,y).
     */
    fun getRegion(point: Location): WorldRegion? {
        return getRegion(point.x, point.y)
    }

    fun getRegion(x: Int, y: Int): WorldRegion? {
        return worldRegions[(x - WORLD_X_MIN) / REGION_SIZE][(y - WORLD_Y_MIN) / REGION_SIZE]
    }

    /**
     * Delete all spawns in the world.
     */
    fun deleteVisibleNpcSpawns() {
        LOGGER.info("Deleting all visible NPCs.")
        for (i in 0..REGIONS_X) {
            for (j in 0..REGIONS_Y) {
                for (obj in worldRegions[i][j]?.objects ?: emptyList()) {
                    if (obj is Npc) {
                        obj.deleteMe()

                        val spawn = obj.spawn
                        if (spawn != null) {
                            spawn.setRespawnState(false)
                            SpawnTable.deleteSpawn(spawn, false)
                        }
                    }
                }
            }
        }
        LOGGER.info("All visibles NPCs are now deleted.")
    }
}