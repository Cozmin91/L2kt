package com.l2kt.gameserver.model.zone.type

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.zone.SpawnZoneType
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.network.SystemMessageId

/**
 * A zone extending [SpawnZoneType], where summoning is forbidden. The place is considered a pvp zone (no flag, no karma). It is used for arenas.
 */
class ArenaZone(id: Int) : SpawnZoneType(id) {

    override fun onEnter(character: Creature) {
        if (character is Player)
            character.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE)

        character.setInsideZone(ZoneId.PVP, true)
        character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true)
    }

    override fun onExit(character: Creature) {
        character.setInsideZone(ZoneId.PVP, false)
        character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false)

        if (character is Player)
            character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE)
    }
}