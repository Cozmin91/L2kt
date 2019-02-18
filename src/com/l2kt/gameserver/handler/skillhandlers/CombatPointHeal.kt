package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.handler.SkillHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.StatusUpdate
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.templates.skills.L2SkillType

class CombatPointHeal : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(actChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        val handler = SkillHandler.getHandler(L2SkillType.BUFF)
        if (handler != null)
            handler!!.useSkill(actChar, skill, targets)

        for (obj in targets) {
            if (obj !is Creature)
                continue

            if (obj.isDead() || obj.isInvul)
                continue

            var cp = skill.power

            if (obj.currentCp + cp >= obj.maxCp)
                cp = obj.maxCp - obj.currentCp

            obj.currentCp = cp + obj.currentCp

            val sump = StatusUpdate(obj)
            sump.addAttribute(StatusUpdate.CUR_CP, obj.currentCp.toInt())
            obj.sendPacket(sump)

            if (obj is Player) {
                if (actChar is Player && actChar !== obj)
                    obj.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.S2_CP_WILL_BE_RESTORED_BY_S1).addCharName(
                            actChar
                        ).addNumber(cp.toInt())
                    )
                else
                    obj.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED).addNumber(cp.toInt()))
            }
        }
    }

    companion object {
        private val SKILL_IDS = arrayOf(L2SkillType.COMBATPOINTHEAL)
    }
}