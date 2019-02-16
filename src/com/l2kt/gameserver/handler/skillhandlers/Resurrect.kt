package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.ShotType
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Pet
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.skills.Formulas
import com.l2kt.gameserver.taskmanager.DecayTaskManager
import com.l2kt.gameserver.templates.skills.L2SkillType

class Resurrect : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        for (cha in targets) {
            val target = cha as Creature
            if (activeChar is Player) {
                if (cha is Player)
                    cha.reviveRequest(activeChar, skill, false)
                else if (cha is Pet) {
                    if (cha.owner == activeChar)
                        target.doRevive(Formulas.calculateSkillResurrectRestorePercent(skill.power, activeChar))
                    else
                        cha.owner.reviveRequest(activeChar, skill, true)
                } else
                    target.doRevive(Formulas.calculateSkillResurrectRestorePercent(skill.power, activeChar))
            } else {
                DecayTaskManager.cancel(target)
                target.doRevive(Formulas.calculateSkillResurrectRestorePercent(skill.power, activeChar))
            }
        }
        activeChar.setChargedShot(
            if (activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT)) ShotType.BLESSED_SPIRITSHOT else ShotType.SPIRITSHOT,
            skill.isStaticReuse
        )
    }

    companion object {
        private val SKILL_IDS = arrayOf(L2SkillType.RESURRECT)
    }
}