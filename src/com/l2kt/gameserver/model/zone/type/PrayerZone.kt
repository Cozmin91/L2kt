package com.l2kt.gameserver.model.zone.type

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.model.zone.ZoneType

/**
 * A zone extending [ZoneType], used for castle's artifacts.<br></br>
 * <br></br>
 * A check forces players to cast on this type of zone, to avoid hiding spots or exploits.
 */
class PrayerZone(id: Int) : ZoneType(id) {

    override fun onEnter(character: Creature) {
        character.setInsideZone(ZoneId.CAST_ON_ARTIFACT, true)
    }

    override fun onExit(character: Creature) {
        character.setInsideZone(ZoneId.CAST_ON_ARTIFACT, false)
    }
}