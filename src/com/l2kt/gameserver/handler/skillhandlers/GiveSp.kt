package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.templates.skills.L2SkillType

class GiveSp : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        val spToAdd = skill.power.toInt()

        for (obj in targets) {
            val target = obj as Creature
            target?.addExpAndSp(0, spToAdd)
        }
    }

    companion object {
        private val SKILL_IDS = arrayOf(L2SkillType.GIVE_SP)
    }
}