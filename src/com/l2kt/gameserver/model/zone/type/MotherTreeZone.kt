package com.l2kt.gameserver.model.zone.type

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.model.zone.ZoneType
import com.l2kt.gameserver.network.serverpackets.SystemMessage

/**
 * A zone extending [ZoneType], used for hp/mp regen boost. Notably used by Mother Tree. It has a Race condition, and allow a entrance and exit message.
 */
class MotherTreeZone(id: Int) : ZoneType(id) {
    private var _enterMsg: Int = 0
    private var _leaveMsg: Int = 0

    var mpRegenBonus = 1
        private set
    var hpRegenBonus = 1
        private set
    private var _race = -1

    override fun setParameter(name: String, value: String) {
        when (name) {
            "enterMsgId" -> _enterMsg = value.toInt()
            "leaveMsgId" -> _leaveMsg = value.toInt()
            "MpRegenBonus" -> mpRegenBonus = value.toInt()
            "HpRegenBonus" -> hpRegenBonus = value.toInt()
            "affectedRace" -> _race = value.toInt()
            else -> super.setParameter(name, value)
        }
    }

    override fun isAffected(character: Creature): Boolean {
        return if (character is Player) _race == character.race!!.ordinal else true

    }

    override fun onEnter(character: Creature) {
        if (character is Player) {
            character.setInsideZone(ZoneId.MOTHER_TREE, true)

            if (_enterMsg != 0)
                character.sendPacket(SystemMessage.getSystemMessage(_enterMsg))
        }
    }

    override fun onExit(character: Creature) {
        if (character is Player) {
            character.setInsideZone(ZoneId.MOTHER_TREE, false)

            if (_leaveMsg != 0)
                character.sendPacket(SystemMessage.getSystemMessage(_leaveMsg))
        }
    }
}