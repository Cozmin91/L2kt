package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.templates.skills.L2SkillType

class DrainSoul : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        // Check player.
        if (activeChar == null || activeChar.isDead() || activeChar !is Player)
            return

        // Check quest condition.
        val st = activeChar.getQuestState(qn)
        if (st == null || !st.isStarted)
            return

        // Get target.
        val target = targets[0]
        if (target == null || target !is Attackable)
            return

        // Check monster.
        if (target.isDead())
            return

        // Range condition, cannot be higher than skill's effectRange.
        if (!activeChar.isInsideRadius(target, skill.effectRange, true, true))
            return

        // Register.
        target.registerAbsorber(activeChar)
    }

    companion object {
        private val qn = "Q350_EnhanceYourWeapon"

        private val SKILL_IDS = arrayOf(L2SkillType.DRAIN_SOUL)
    }
}