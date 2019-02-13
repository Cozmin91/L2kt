package com.l2kt.gameserver.model.zone.type

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.model.zone.ZoneType

/**
 * A zone extending [ZoneType], used to restrict [Player]s to enter mounted on wyverns.<br></br>
 * <br></br>
 * A task and a message is called if event is triggered. If the player didn't leave after 5 seconds, he will be dismounted.
 */
class NoLandingZone(id: Int) : ZoneType(id) {

    override fun onEnter(character: Creature) {
        if (character is Player) {
            character.setInsideZone(ZoneId.NO_LANDING, true)

            character.enterOnNoLandingZone()
        }
    }

    override fun onExit(character: Creature) {
        if (character is Player) {
            character.setInsideZone(ZoneId.NO_LANDING, false)

            character.exitOnNoLandingZone()
        }
    }
}