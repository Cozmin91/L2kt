package com.l2kt.gameserver.model.zone.type

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.model.zone.ZoneType

/**
 * A zone extending [ZoneType] where restart isn't allowed.
 */
class NoRestartZone(id: Int) : ZoneType(id) {

    override fun onEnter(character: Creature) {
        if (character is Player)
            character.setInsideZone(ZoneId.NO_RESTART, true)
    }

    override fun onExit(character: Creature) {
        if (character is Player)
            character.setInsideZone(ZoneId.NO_RESTART, false)
    }
}