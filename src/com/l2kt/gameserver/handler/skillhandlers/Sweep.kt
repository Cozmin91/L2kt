package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.templates.skills.L2SkillType

class Sweep : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        if (activeChar !is Player)
            return

        for (target in targets) {
            if (target !is Attackable)
                continue

            if (!target.isSpoiled)
                continue

            val items = target.sweepItems
            if (items.isEmpty())
                continue

            for (item in items) {
                if (activeChar.isInParty)
                    activeChar.party!!.distributeItem(activeChar, item, true, target)
                else
                    activeChar.addItem("Sweep", item.id, item.value, activeChar, true)
            }
        }
    }

    companion object {
        private val SKILL_IDS = arrayOf(L2SkillType.SWEEP)
    }
}