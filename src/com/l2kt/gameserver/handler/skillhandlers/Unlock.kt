package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Chest
import com.l2kt.gameserver.model.actor.instance.Door

import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.templates.skills.L2SkillType

class Unlock : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        val `object` = targets[0]

        if (`object` is Door) {
            if (!`object`.isUnlockable && skill.skillType !== L2SkillType.UNLOCK_SPECIAL) {
                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.UNABLE_TO_UNLOCK_DOOR))
                return
            }

            if (doorUnlock(skill) && !`object`.isOpened)
                `object`.openMe()
            else
                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_UNLOCK_DOOR))
        } else if (`object` is Chest) {
            if (`object`.isDead || `object`.isInteracted)
                return

            `object`.setInteracted()
            if (chestUnlock(skill, `object`)) {
                `object`.setSpecialDrop()
                `object`.doDie(null)
            } else {
                `object`.addDamageHate(activeChar, 0, 999)
                `object`.ai.setIntention(CtrlIntention.ATTACK, activeChar)
            }
        } else
            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET))
    }

    companion object {
        private val SKILL_IDS = arrayOf(L2SkillType.UNLOCK, L2SkillType.UNLOCK_SPECIAL)

        private fun doorUnlock(skill: L2Skill): Boolean {
            if (skill.skillType === L2SkillType.UNLOCK_SPECIAL)
                return Rnd[100] < skill.power

            when (skill.level) {
                0 -> return false
                1 -> return Rnd[120] < 30
                2 -> return Rnd[120] < 50
                3 -> return Rnd[120] < 75
                else -> return Rnd[120] < 100
            }
        }

        private fun chestUnlock(skill: L2Skill, chest: Creature): Boolean {
            var chance = 0
            if (chest.level > 60) {
                if (skill.level < 10)
                    return false

                chance = (skill.level - 10) * 5 + 30
            } else if (chest.level > 40) {
                if (skill.level < 6)
                    return false

                chance = (skill.level - 6) * 5 + 10
            } else if (chest.level > 30) {
                if (skill.level < 3)
                    return false

                if (skill.level > 12)
                    return true

                chance = (skill.level - 3) * 5 + 30
            } else {
                if (skill.level > 10)
                    return true

                chance = skill.level * 5 + 35
            }

            chance = Math.min(chance, 50)
            return Rnd[100] < chance
        }
    }
}