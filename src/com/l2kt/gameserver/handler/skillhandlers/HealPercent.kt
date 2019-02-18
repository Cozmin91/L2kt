package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.handler.SkillHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Door
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.instance.SiegeFlag
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.StatusUpdate
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.templates.skills.L2SkillType

class HealPercent : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        val handler = SkillHandler.getHandler(L2SkillType.BUFF)
        if (handler != null)
            handler!!.useSkill(activeChar, skill, targets)

        var hp = false
        var mp = false

        when (skill.skillType) {
            L2SkillType.HEAL_PERCENT -> hp = true

            L2SkillType.MANAHEAL_PERCENT -> mp = true
        }

        var su: StatusUpdate? = null
        var sm: SystemMessage
        var amount = 0.0
        val full = skill.power == 100.0
        var targetPlayer = false

        for (obj in targets) {
            if (obj !is Creature)
                continue

            if (obj.isDead() || obj.isInvul)
                continue

            // Doors and flags can't be healed in any way
            if (obj is Door || obj is SiegeFlag)
                continue

            targetPlayer = obj is Player

            // Cursed weapon owner can't heal or be healed
            if (obj !== activeChar) {
                if (activeChar is Player && activeChar.isCursedWeaponEquipped)
                    continue

                if (targetPlayer && (obj as Player).isCursedWeaponEquipped)
                    continue
            }

            if (hp) {
                amount = Math.min(if (full) obj.maxHp.toDouble() else obj.maxHp * skill.power / 100.0, obj.maxHp - obj.currentHp)
                obj.currentHp = amount + obj.currentHp
            } else if (mp) {
                amount = Math.min(if (full) obj.maxMp.toDouble() else obj.maxMp * skill.power / 100.0, obj.maxMp - obj.currentMp)
                obj.currentMp = amount + obj.currentMp
            }

            if (targetPlayer) {
                su = StatusUpdate(obj)

                if (hp) {
                    if (activeChar !== obj)
                        sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HP_RESTORED_BY_S1)
                            .addCharName(activeChar)
                    else
                        sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED)

                    sm.addNumber(amount.toInt())
                    obj.sendPacket(sm)
                    su.addAttribute(StatusUpdate.CUR_HP, obj.currentHp.toInt())
                } else if (mp) {
                    if (activeChar !== obj)
                        sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MP_RESTORED_BY_S1)
                            .addCharName(activeChar)
                    else
                        sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MP_RESTORED)

                    sm.addNumber(amount.toInt())
                    obj.sendPacket(sm)
                    su.addAttribute(StatusUpdate.CUR_MP, obj.currentMp.toInt())
                }

                obj.sendPacket(su)
            }
        }
    }

    companion object {
        private val SKILL_IDS = arrayOf(L2SkillType.HEAL_PERCENT, L2SkillType.MANAHEAL_PERCENT)
    }
}