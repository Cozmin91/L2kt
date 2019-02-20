package com.l2kt.gameserver.model.actor.status

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.entity.Duel.DuelState

open class NpcStatus(activeChar: Npc) : CreatureStatus(activeChar) {

    override fun reduceHp(value: Double, attacker: Creature) {
        reduceHp(value, attacker, true, false, false)
    }

    override fun reduceHp(
        value: Double,
        attacker: Creature?,
        awake: Boolean,
        isDOT: Boolean,
        isHpConsumption: Boolean
    ) {
        if (activeChar.isDead)
            return

        if (attacker != null) {
            val attackerPlayer = attacker.actingPlayer
            if (attackerPlayer != null && attackerPlayer.isInDuel)
                attackerPlayer.duelState = DuelState.INTERRUPTED
        }

        super.reduceHp(value, attacker, awake, isDOT, isHpConsumption)
    }

    override val activeChar: Npc
        get() = super.activeChar as Npc
}