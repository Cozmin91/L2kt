package com.l2kt.gameserver.model.zone.type

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.model.zone.ZoneType

/**
 * A zone extending [ZoneType] used by Derby Track system.<br></br>
 * <br></br>
 * The zone shares peace, no summon and monster track behaviors.
 */
class DerbyTrackZone(id: Int) : ZoneType(id) {

    override fun onEnter(character: Creature) {
        if (character is Playable) {
            character.setInsideZone(ZoneId.MONSTER_TRACK, true)
            character.setInsideZone(ZoneId.PEACE, true)
            character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true)
        }
    }

    override fun onExit(character: Creature) {
        if (character is Playable) {
            character.setInsideZone(ZoneId.MONSTER_TRACK, false)
            character.setInsideZone(ZoneId.PEACE, false)
            character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false)
        }
    }
}