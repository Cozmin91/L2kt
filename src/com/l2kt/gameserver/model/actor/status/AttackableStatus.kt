package com.l2kt.gameserver.model.actor.status

import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature

class AttackableStatus(activeChar: Attackable) : NpcStatus(activeChar) {

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

        if (value > 0) {
            if (activeChar.isOverhit)
                activeChar.setOverhitValues(attacker, value)
            else
                activeChar.overhitEnabled(false)
        } else
            activeChar.overhitEnabled(false)

        // Add attackers to npc's attacker list
        if (attacker != null)
            activeChar.addAttackerToAttackByList(attacker)

        super.reduceHp(value, attacker, awake, isDOT, isHpConsumption)

        // And the attacker's hit didn't kill the mob, clear the over-hit flag
        if (!activeChar.isDead)
            activeChar.overhitEnabled(false)
    }

    override val activeChar: Attackable
        get() = super.activeChar as Attackable
}