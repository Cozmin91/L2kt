package com.l2kt.gameserver.data.sql

import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.logging.CLogger
import com.l2kt.gameserver.data.sql.PlayerInfoTable.PlayerInfo
import com.l2kt.gameserver.model.actor.instance.Player
import java.util.concurrent.ConcurrentHashMap

/**
 * This class caches few [Player]s informations. It keeps a link between objectId and a [PlayerInfo].
 *
 *
 * It is notably used for any offline character check, such as friendlist, existing character name, etc.
 *
 */
object PlayerInfoTable {

    private val _infos = ConcurrentHashMap<Int, PlayerInfo>()
    private val LOGGER = CLogger(PlayerInfoTable::class.java.name)

    private const val LOAD_DATA = "SELECT account_name, obj_Id, char_name, accesslevel FROM characters"

    init {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(LOAD_DATA).use { ps ->
                    ps.executeQuery().use { rs ->
                        while (rs.next())
                            _infos[rs.getInt("obj_Id")] =
                                PlayerInfo(
                                    rs.getString("account_name"),
                                    rs.getString("char_name"),
                                    rs.getInt("accesslevel")
                                )
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't load player infos.", e)
        }

        LOGGER.info("Loaded {} player infos.", _infos.size)
    }

    /**
     * Caches [Player] informations, but only if not already existing.
     * @param objectId : The player's objectId.
     * @param accountName : The player's account name.
     * @param playerName : The player's name.
     * @param accessLevel : The player's access level.
     */
    fun addPlayer(objectId: Int, accountName: String, playerName: String, accessLevel: Int) {
        (_infos).putIfAbsent(
            objectId,
            PlayerInfo(accountName, playerName, accessLevel)
        )
    }

    /**
     * Update the [Player] data. The informations must already exist. Used for name and access level edition.
     * @param player : The player to update.
     * @param onlyAccessLevel : If true, it will update the access level, otherwise, it will update the player name.
     */
    fun updatePlayerData(player: Player?, onlyAccessLevel: Boolean) {
        if (player == null)
            return

        val data = _infos[player.objectId]
        if (data != null) {
            if (onlyAccessLevel)
                data.accessLevel = player.accessLevel.level
            else {
                val playerName = player.name
                if (!data.playerName!!.equals(playerName, ignoreCase = true))
                    data.playerName = playerName
            }
        }
    }

    /**
     * Remove a [Player] entry.
     * @param objId : The objectId to check.
     */
    fun removePlayer(objId: Int) {
        if (_infos.containsKey(objId))
            _infos.remove(objId)
    }

    /**
     * Get [Player] objectId by name (reversing call).
     * @param playerName : The name to check.
     * @return the player objectId.
     */
    fun getPlayerObjectId(playerName: String): Int {
        return if (playerName.isEmpty()) -1 else _infos.entries
            .filter { m -> m.value.playerName.equals(playerName, ignoreCase = true) }
            .map{ it.key }.firstOrNull() ?: -1
    }

    /**
     * Get [Player] name by object id.
     * @param objId : The objectId to check.
     * @return the player name.
     */
    fun getPlayerName(objId: Int): String? {
        val data = _infos[objId]
        return data?.playerName
    }

    /**
     * Get [Player] access level by object id.
     * @param objId : The objectId to check.
     * @return the access level.
     */
    fun getPlayerAccessLevel(objId: Int): Int {
        val data = _infos[objId]
        return data?.accessLevel ?: 0
    }

    /**
     * Retrieve characters amount from any account, by account name.
     * @param accountName : The account name to check.
     * @return the number of characters stored into this account.
     */
    fun getCharactersInAcc(accountName: String): Int {
        return _infos.entries.filter { m -> m.value.accountName.equals(accountName, ignoreCase = true) }
            .count()
    }

    /**
     * A datatype used to retain Player informations such as account name, player name and access level.
     */
    private data class PlayerInfo(val accountName: String, var playerName: String?, var accessLevel: Int)
}