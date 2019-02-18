package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.ai.CtrlEvent
import com.l2kt.gameserver.model.actor.instance.Monster
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.skills.Formulas
import com.l2kt.gameserver.templates.skills.L2SkillType

class Spoil : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        if (activeChar !is Player)
            return

        if (targets.isEmpty())
            return

        for (tgt in targets) {
            if (tgt !is Monster)
                continue

            if (tgt.isDead())
                continue

            if (tgt.spoilerId != 0) {
                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_SPOILED))
                continue
            }

            if (Formulas.calcMagicSuccess(activeChar, tgt as Creature, skill)) {
                tgt.spoilerId = activeChar.objectId
                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SPOIL_SUCCESS))
            } else
                activeChar.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(
                        tgt
                    ).addSkillName(skill.id)
                )

            tgt.ai.notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar)
        }
    }

    companion object {
        private val SKILL_IDS = arrayOf(L2SkillType.SPOIL)
    }
}