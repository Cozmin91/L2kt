package com.l2kt.gameserver.model.zone.type

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.zone.CastleZoneType
import com.l2kt.gameserver.model.zone.ZoneId

/**
 * A zone extending [CastleZoneType], which fires a task on the first character entrance, notably used by castle slow traps.<br></br>
 * <br></br>
 * This task slows down [Player]s.
 */
class SwampZone(id: Int) : CastleZoneType(id) {
    var moveBonus = -50
        private set

    override fun setParameter(name: String, value: String) {
        if (name == "move_bonus")
            moveBonus = Integer.parseInt(value)
        else
            super.setParameter(name, value)
    }

    override fun onEnter(character: Creature) {
        // Castle traps are active only during siege, or if they're activated.
        if (castle != null && (!isEnabled || !castle!!.siege.isInProgress))
            return

        character.setInsideZone(ZoneId.SWAMP, true)
        if (character is Player)
            character.broadcastUserInfo()
    }

    override fun onExit(character: Creature) {
        character.setInsideZone(ZoneId.SWAMP, false)
        if (character is Player)
            character.broadcastUserInfo()
    }
}