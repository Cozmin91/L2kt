package com.l2kt.gameserver.model.zone.type

import com.l2kt.L2DatabaseFactory
import com.l2kt.gameserver.data.xml.MapRegionData
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.Summon
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.model.zone.ZoneType
import java.util.concurrent.ConcurrentHashMap

/**
 * A Boss zone, extending [ZoneType]. It holds a [List] and a [Map] of allowed [Player]s.<br></br>
 * <br></br>
 * The Map is used for Players disconnections, while the List is used for Players to re-enter the zone after server downtime/restart.
 */
class BossZone(id: Int) : ZoneType(id) {

    // Track the times that players got disconnected. Players are allowed to log back into the zone as long as their log-out was within _timeInvade time...
    private val _allowedPlayersEntryTime = ConcurrentHashMap<Int, Long>()

    // Track players admitted to the zone who should be allowed back in after reboot/server downtime, within 30min of server restart
    private val _allowedPlayers = ConcurrentHashMap.newKeySet<Int>()

    private val _oustLoc = IntArray(3)

    private var _invadeTime: Int = 0

    /**
     * @return the list of all allowed [Player]s objectIds.
     */
    val allowedPlayers: Set<Int>
        get() = _allowedPlayers

    init {

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(SELECT_GRAND_BOSS_LIST).use { ps ->
                    ps.setInt(1, id)

                    ps.executeQuery().use { rs ->
                        while (rs.next())
                            allowPlayerEntry(rs.getInt("player_id"))
                    }
                }
            }
        } catch (e: Exception) {
            ZoneType.Companion.LOGGER.error("Couldn't load players for {}.", e, toString())
        }

    }

    override fun setParameter(name: String, value: String) {
        if (name == "InvadeTime")
            _invadeTime = Integer.parseInt(value)
        else if (name == "oustX")
            _oustLoc[0] = Integer.parseInt(value)
        else if (name == "oustY")
            _oustLoc[1] = Integer.parseInt(value)
        else if (name == "oustZ")
            _oustLoc[2] = Integer.parseInt(value)
        else
            super.setParameter(name, value)
    }

    override fun onEnter(character: Creature) {
        character.setInsideZone(ZoneId.BOSS, true)

        if (character is Player) {
            // Get player and set zone info.
            character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true)

            // Skip other checks for GM or if no invade time is set.
            if (character.isGM || _invadeTime == 0)
                return

            // Get player object id.
            val id = character.objectId

            if (_allowedPlayers.contains(id)) {
                // Get and remove the entry expiration time (once entered, can not enter enymore, unless specified).
                val entryTime = _allowedPlayersEntryTime.remove(id)!!
                if (entryTime > System.currentTimeMillis())
                    return

                // Player trying to join after expiration, remove from allowed list.
                _allowedPlayers.remove(Integer.valueOf(id))
            }

            // Teleport out player, who attempt "illegal" (re-)entry.
            if (_oustLoc[0] != 0 && _oustLoc[1] != 0 && _oustLoc[2] != 0)
                character.teleToLocation(_oustLoc[0], _oustLoc[1], _oustLoc[2], 0)
            else
                character.teleToLocation(MapRegionData.TeleportType.TOWN)
        } else if (character is Summon) {
            val player = character.owner
            if (player != null) {
                if (_allowedPlayers.contains(player.objectId) || player.isGM || _invadeTime == 0)
                    return

                // Remove summon.
                character.unSummon(player)
            }
        }
    }

    override fun onExit(character: Creature) {
        character.setInsideZone(ZoneId.BOSS, false)

        if (character is Playable) {
            if (character is Player) {
                // Get player and set zone info.
                character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false)

                // Skip other checks for GM or if no invade time is set.
                if (character.isGM || _invadeTime == 0)
                    return

                // Get player object id.
                val id = character.objectId

                if (_allowedPlayers.contains(id)) {
                    if (!character.isOnline) {
                        // Player disconnected.
                        _allowedPlayersEntryTime[id] = System.currentTimeMillis() + _invadeTime
                    } else {
                        // Player has allowed entry, do not delete from allowed list.
                        if (_allowedPlayersEntryTime.containsKey(id))
                            return

                        // Remove player allowed list.
                        _allowedPlayers.remove(Integer.valueOf(id))
                    }
                }
            }

            // If playables aren't found, force all bosses to return to spawnpoint.
            if (!_characters.isEmpty()) {
                if (!getKnownTypeInside(Playable::class.java).isEmpty())
                    return

                for (raid in getKnownTypeInside(Attackable::class.java)) {
                    if (!raid.isRaidRelated)
                        continue

                    raid.returnHome()
                }
            }
        } else if (character is Attackable && character.isRaidRelated())
            character.returnHome()
    }

    /**
     * Enables the entry of a [Player] to this [BossZone] for next "duration" seconds. If the Player tries to enter the zone after this period, he will be teleported out.
     * @param player : The allowed player to entry.
     * @param duration : The entry permission period (in seconds).
     */
    fun allowPlayerEntry(player: Player, duration: Int) {
        // Get player object id.
        val playerId = player.objectId

        // Allow player entry.
        if (!_allowedPlayers.contains(playerId))
            _allowedPlayers.add(playerId)

        // For the given duration.
        _allowedPlayersEntryTime[playerId] = System.currentTimeMillis() + duration * 1000
    }

    /**
     * Enables the entry of a [Player] to this [BossZone] after server shutdown/restart. The time limit is specified by each zone via "InvadeTime" parameter. If the player tries to enter the zone after this period, he will be teleported out.
     * @param playerId : The objectid of the allowed player to entry.
     */
    fun allowPlayerEntry(playerId: Int) {
        // Allow player entry.
        if (!_allowedPlayers.contains(playerId))
            _allowedPlayers.add(playerId)

        // For the given duration.
        _allowedPlayersEntryTime[playerId] = System.currentTimeMillis() + _invadeTime
    }

    /**
     * Removes the [Player] from allowed list and cancel the entry permition.
     * @param player : Player to remove from the zone.
     */
    fun removePlayer(player: Player) {
        // Get player object id.
        val id = player.objectId

        // Remove player from allowed list.
        _allowedPlayers.remove(Integer.valueOf(id))

        // Remove player permission.
        _allowedPlayersEntryTime.remove(id)
    }

    /**
     * Teleport all [Player]s located in this [BossZone] to a specific location, as listed on [._oustLoc]. Clear both containers holding Players informations.
     * @return the List of all Players who have been forced to teleport.
     */
    fun oustAllPlayers(): List<Player> {
        if (_characters.isEmpty())
            return emptyList()

        val players = getKnownTypeInside(Player::class.java)

        for (player in players) {
            if (player.isOnline) {
                if (_oustLoc[0] != 0 && _oustLoc[1] != 0 && _oustLoc[2] != 0)
                    player.teleToLocation(_oustLoc[0], _oustLoc[1], _oustLoc[2], 0)
                else
                    player.teleToLocation(MapRegionData.TeleportType.TOWN)
            }
        }
        _allowedPlayersEntryTime.clear()
        _allowedPlayers.clear()

        return players
    }

    companion object {
        private const val SELECT_GRAND_BOSS_LIST = "SELECT * FROM grandboss_list WHERE zone = ?"
    }
}