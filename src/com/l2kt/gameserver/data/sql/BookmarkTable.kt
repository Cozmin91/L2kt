package com.l2kt.gameserver.data.sql

import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.logging.CLogger
import com.l2kt.gameserver.model.Bookmark
import com.l2kt.gameserver.model.actor.instance.Player
import java.util.*

/**
 * This class loads and handles [Bookmark] into a List.<br></br>
 * To retrieve a Bookmark, you need its name and the player objectId.
 */
object BookmarkTable {

    private val _bks = ArrayList<Bookmark>()
    private val LOGGER = CLogger(BookmarkTable::class.java.name)

    init {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement("SELECT * FROM bookmarks").use { ps ->
                    ps.executeQuery().use { rs ->
                        while (rs.next())
                            _bks.add(
                                Bookmark(
                                    rs.getString("name"),
                                    rs.getInt("obj_Id"),
                                    rs.getInt("x"),
                                    rs.getInt("y"),
                                    rs.getInt("z")
                                )
                            )
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't restore bookmarks.", e)
        }

        LOGGER.info("Loaded {} bookmarks.", _bks.size)
    }

    /**
     * Verify if a [Bookmark] already exists.
     * @param name : The Bookmark name.
     * @param objId : The [Player] objectId to make checks on.
     * @return true if the Bookmark exists, false otherwise.
     */
    fun isExisting(name: String, objId: Int): Boolean {
        return getBookmark(name, objId) != null
    }

    /**
     * Retrieve a [Bookmark] by its name and its specific [Player] objectId.
     * @param name : The Bookmark name.
     * @param objId : The Player objectId to make checks on.
     * @return the Bookmark if it exists, null otherwise.
     */
    fun getBookmark(name: String, objId: Int): Bookmark? {
        for (bk in _bks) {
            if (bk.name.equals(name, ignoreCase = true) && bk.id == objId)
                return bk
        }
        return null
    }

    /**
     * @param objId : The Player objectId to make checks on.
     * @return the list of [Bookmark]s of a [Player].
     */
    fun getBookmarks(objId: Int): List<Bookmark> {
        return _bks.filter { bk -> bk.id == objId }
    }

    /**
     * Creates a new [Bookmark] and store info to database.
     * @param name : The name of the Bookmark.
     * @param player : The [Player] who requested the creation. We use the Player location as Bookmark location.
     */
    fun saveBookmark(name: String, player: Player) {
        val objId = player.objectId
        val x = player.x
        val y = player.y
        val z = player.z

        _bks.add(Bookmark(name, objId, x, y, z))

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement("INSERT INTO bookmarks (name, obj_Id, x, y, z) values (?,?,?,?,?)").use { ps ->
                    ps.setString(1, name)
                    ps.setInt(2, objId)
                    ps.setInt(3, x)
                    ps.setInt(4, y)
                    ps.setInt(5, z)
                    ps.execute()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't save bookmark.", e)
        }

    }

    /**
     * Delete a [Bookmark], based on the [Player] objectId and its name.
     * @param name : The name of the Bookmark.
     * @param objId : The Player objectId to make checks on.
     */
    fun deleteBookmark(name: String, objId: Int) {
        val bookmark = getBookmark(name, objId)
        if (bookmark != null) {
            _bks.remove(bookmark)

            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement("DELETE FROM bookmarks WHERE name=? AND obj_Id=?").use { ps ->
                        ps.setString(1, name)
                        ps.setInt(2, objId)
                        ps.execute()
                    }
                }
            } catch (e: Exception) {
                LOGGER.error("Couldn't delete bookmark.", e)
            }

        }
    }
}