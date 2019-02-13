package com.l2kt.gameserver.model.zone.type

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.model.zone.ZoneType

/**
 * A zone extending [ZoneType], used for jail behavior. It is impossible to summon friends and use shops inside it.
 */
class JailZone(id: Int) : ZoneType(id) {

    override fun onEnter(character: Creature) {
        if (character is Player) {
            character.setInsideZone(ZoneId.JAIL, true)
            character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true)
            character.setInsideZone(ZoneId.NO_STORE, true)
        }
    }

    override fun onExit(character: Creature) {
        if (character is Player) {
            character.setInsideZone(ZoneId.JAIL, false)
            character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false)
            character.setInsideZone(ZoneId.NO_STORE, false)
        }
    }
}