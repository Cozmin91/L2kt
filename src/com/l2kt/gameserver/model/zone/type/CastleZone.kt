package com.l2kt.gameserver.model.zone.type

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.zone.SpawnZoneType
import com.l2kt.gameserver.model.zone.ZoneId

/**
 * A zone extending [SpawnZoneType] which handles following spawns type :
 *
 *  * Generic spawn locs : owner_restart_point_list (spawns used on siege, to respawn on mass gatekeeper room.
 *  * Chaotic spawn locs : banish_point_list (spawns used to banish players on regular owner maintenance).
 *
 */
class CastleZone(id: Int) : SpawnZoneType(id) {
    var castleId: Int = 0
        private set

    override fun setParameter(name: String, value: String) {
        if (name == "castleId")
            castleId = Integer.parseInt(value)
        else
            super.setParameter(name, value)
    }

    override fun onEnter(character: Creature) {
        character.setInsideZone(ZoneId.CASTLE, true)
    }

    override fun onExit(character: Creature) {
        character.setInsideZone(ZoneId.CASTLE, false)
    }

    /**
     * Kick [Player]s who don't belong to the clan set as parameter from this zone. They are ported to a random "chaotic" location.
     * @param clanId : The castle owner clanId. Related players aren't teleported out.
     */
    fun banishForeigners(clanId: Int) {
        if (_characters.isEmpty())
            return

        for (player in getKnownTypeInside(Player::class.java)) {
            if (player.clanId == clanId)
                continue

            player.teleToLocation(randomChaoticLoc, 20)
        }
    }
}