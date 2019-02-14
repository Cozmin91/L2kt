package com.l2kt.gameserver.model.zone.type

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.model.zone.ZoneType

/**
 * A zone extending [ZoneType] used for Mass Gatekeepers to teleport players on a specific location.<br></br>
 * <br></br>
 * Summoning is forbidden. It holds a location under an int array, and castleId.
 */
class CastleTeleportZone(id: Int) : ZoneType(id) {
    private val _spawnLoc: IntArray = IntArray(5)
    var castleId: Int = 0
        private set

    override fun setParameter(name: String, value: String) {
        when (name) {
            "castleId" -> castleId = value.toInt()
            "spawnMinX" -> _spawnLoc[0] = value.toInt()
            "spawnMaxX" -> _spawnLoc[1] = value.toInt()
            "spawnMinY" -> _spawnLoc[2] = value.toInt()
            "spawnMaxY" -> _spawnLoc[3] = value.toInt()
            "spawnZ" -> _spawnLoc[4] = value.toInt()
            else -> super.setParameter(name, value)
        }
    }

    override fun onEnter(character: Creature) {
        character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true)
    }

    override fun onExit(character: Creature) {
        character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false)
    }

    fun oustAllPlayers() {
        if (_characters.isEmpty())
            return

        for (player in getKnownTypeInside(Player::class.java))
            player.teleToLocation(Rnd[_spawnLoc[0], _spawnLoc[1]], Rnd[_spawnLoc[2], _spawnLoc[3]], _spawnLoc[4], 0)
    }
}