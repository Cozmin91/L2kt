package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.templates.skills.L2SkillType

class Dummy : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        if (activeChar !is Player)
            return

        if (skill.skillType === L2SkillType.BEAST_FEED) {
            val target = targets[0] ?: return
        }
    }

    companion object {
        private val SKILL_IDS = arrayOf(L2SkillType.DUMMY, L2SkillType.BEAST_FEED, L2SkillType.DELUXE_KEY_UNLOCK)
    }
}