package com.l2kt.gameserver.data.manager

import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.logging.CLogger
import com.l2kt.gameserver.model.actor.instance.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.ToIntFunction

/**
 * Loads and stores Raid Points.<br></br>
 * <br></br>
 * [Player]s obtain Raid Points by killing Raid Bosses within the world of Lineage 2. Each week the Top ranked players clans get rewarded with Clan Reputation Points in relation to their members ranking.
 */
object RaidPointManager {

    private val _entries = ConcurrentHashMap<Int, MutableMap<Int, Int>>()
    private val LOGGER = CLogger(RaidPointManager::class.java.name)

    private const val LOAD_DATA = "SELECT * FROM character_raid_points"
    private const val INSERT_DATA = "REPLACE INTO character_raid_points (char_id,boss_id,points) VALUES (?,?,?)"
    private const val DELETE_DATA = "TRUNCATE TABLE character_raid_points"

    /**
     * A method used to generate the ranking map. Limited to the first 100 players.
     *
     *  * Retrieve player data, compute their points and store the entry on a temporary map if the points are > 0.
     *  * Sort the temporary map based on points and feed a second map with objectId<>ranking.
     *
     * @return a [Map] consisting of [Player] objectId for the key, and ranking for the value.
     */
    // Iterate the global list, retrieve players and associated cumulated points. Store them if > 0.
    // Sort the temporary points map, then feed rankMap with objectId as key and ranking as value.
    val winners: Map<Int, Int>
        get() {
            val playersData = HashMap<Int, Int>()
            for (objectId in _entries.keys) {
                val points = getPointsByOwnerId(objectId)
                if (points > 0)
                    playersData[objectId] = points
            }

            val counter = AtomicInteger(1)

            val rankMap = LinkedHashMap<Int, Int>()
            playersData.entries
                .sortedByDescending { (x,y) -> y }.takeLast(100)
                .forEach{e -> rankMap[e.key] = counter.getAndIncrement() }

            return rankMap
        }

    init {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(LOAD_DATA).use { ps ->
                    ps.executeQuery().use { rs ->
                        while (rs.next()) {
                            val objectId = rs.getInt("char_id")
                            val bossId = rs.getInt("boss_id")
                            val points = rs.getInt("points")

                            var playerData: MutableMap<Int, Int> = _entries[objectId]!!

                            playerData[bossId] = points
                            _entries[objectId] = playerData
                        }
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't load RaidPoints entries.", e)
        }

        LOGGER.info("Loaded {} RaidPoints entries.", _entries.size)
    }

    fun getList(player: Player): Map<Int, Int> {
        return _entries[player.objectId].orEmpty()
    }

    /**
     * Add points for a given [Player] and a given boss npcId.
     * @param player : The player used for objectId.
     * @param bossId : The boss npcId to register.
     * @param points : The points to add.
     */
    fun addPoints(player: Player, bossId: Int, points: Int) {
        val objectId = player.objectId

        var playerData: MutableMap<Int, Int>? = _entries[objectId]
        if (playerData == null) {
            playerData = HashMap()

            _entries[objectId] = playerData
        }
        (playerData).merge(
            bossId,
            points
        ) { a, b -> Integer.sum(a, b) }

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(INSERT_DATA).use { ps ->
                    ps.setInt(1, objectId)
                    ps.setInt(2, bossId)
                    ps.setInt(3, points)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't update RaidPoints entries.", e)
        }

    }

    /**
     * @param objectId : The objectId of the [Player] to check.
     * @return the cumulated amount of points for a player, or 0 if no entry is found.
     */
    fun getPointsByOwnerId(objectId: Int): Int {
        val playerData = _entries[objectId]
        return if (playerData == null || playerData.isEmpty()) 0 else playerData.values.stream()
            .mapToInt(ToIntFunction<Int> { it.toInt() }).sum()

    }

    /**
     * Clean both database and memory from content.
     */
    fun cleanUp() {
        _entries.clear()

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(DELETE_DATA).use { ps -> ps.executeUpdate() }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't delete RaidPoints entries.", e)
        }

    }

    /**
     * @param objectId : The objectId of the [Player] to check.
     * @return the current rank of a player based on its objectId, or 0 if no entry is found.
     */
    fun calculateRanking(objectId: Int): Int {
        // Iterate the global list, retrieve players and associated cumulated points. Store them if > 0.
        val playersData = HashMap<Int, Int>()
        for (ownerId in _entries.keys) {
            val points = getPointsByOwnerId(ownerId)
            if (points > 0)
                playersData[ownerId] = points
        }

        val counter = AtomicInteger(1)

        val rankMap = LinkedHashMap<Int, Int>()

        // Sort the temporary points map, then feed rankMap with objectId as key and ranking as value.
        playersData.entries
            .sortedByDescending { (x,y)-> y }
            .forEach{e -> rankMap[e.key] = counter.getAndIncrement()}

        val rank = rankMap[objectId]
        return rank ?: 0
    }
}