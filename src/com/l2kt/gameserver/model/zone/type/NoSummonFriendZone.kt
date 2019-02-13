package com.l2kt.gameserver.model.zone.type

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.model.zone.ZoneType

/**
 * A zone extending [ZoneType] where the use of "summoning friend" skill isn't allowed.
 */
class NoSummonFriendZone(id: Int) : ZoneType(id) {

    override fun onEnter(character: Creature) {
        character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true)
    }

    override fun onExit(character: Creature) {
        character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false)
    }
}