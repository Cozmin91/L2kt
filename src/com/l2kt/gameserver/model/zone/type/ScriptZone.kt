package com.l2kt.gameserver.model.zone.type

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.model.zone.ZoneType

/**
 * A zone extending [ZoneType], used for quests and custom scripts.
 */
class ScriptZone(id: Int) : ZoneType(id) {

    override fun onEnter(character: Creature) {
        character.setInsideZone(ZoneId.SCRIPT, true)
    }

    override fun onExit(character: Creature) {
        character.setInsideZone(ZoneId.SCRIPT, false)
    }
}