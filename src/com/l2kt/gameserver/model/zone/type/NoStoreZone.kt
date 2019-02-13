package com.l2kt.gameserver.model.zone.type

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.model.zone.ZoneType

/**
 * A zone extending [ZoneType] where store isn't allowed.
 */
class NoStoreZone(id: Int) : ZoneType(id) {

    override fun onEnter(character: Creature) {
        if (character is Player)
            character.setInsideZone(ZoneId.NO_STORE, true)
    }

    override fun onExit(character: Creature) {
        if (character is Player)
            character.setInsideZone(ZoneId.NO_STORE, false)
    }
}