package com.l2kt.gameserver.model.zone.type

import com.l2kt.Config
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.zone.SpawnZoneType
import com.l2kt.gameserver.model.zone.ZoneId

/**
 * A zone extending [SpawnZoneType], used by towns. A town zone is generally associated to a castle for taxes.
 */
class TownZone(id: Int) : SpawnZoneType(id) {
    var townId: Int = 0
        private set
    var castleId: Int = 0
        private set

    var isPeaceZone = true
        private set

    override fun setParameter(name: String, value: String) {
        if (name == "townId")
            townId = Integer.parseInt(value)
        else if (name == "castleId")
            castleId = Integer.parseInt(value)
        else if (name == "isPeaceZone")
            isPeaceZone = java.lang.Boolean.parseBoolean(value)
        else
            super.setParameter(name, value)
    }

    override fun onEnter(character: Creature) {
        if (Config.ZONE_TOWN == 1 && character is Player && character.siegeState.toInt() != 0)
            return

        if (isPeaceZone && Config.ZONE_TOWN != 2)
            character.setInsideZone(ZoneId.PEACE, true)

        character.setInsideZone(ZoneId.TOWN, true)
    }

    override fun onExit(character: Creature) {
        if (isPeaceZone)
            character.setInsideZone(ZoneId.PEACE, false)

        character.setInsideZone(ZoneId.TOWN, false)
    }
}