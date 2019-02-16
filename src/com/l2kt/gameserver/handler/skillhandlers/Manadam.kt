package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.ShotType
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Formulas
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.StatusUpdate
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.templates.skills.L2SkillType

class Manadam : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        if (activeChar.isAlikeDead)
            return

        val sps = activeChar.isChargedShot(ShotType.SPIRITSHOT)
        val bsps = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT)

        for (obj in targets) {
            if (obj !is Creature)
                continue

            var target = obj
            if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
                target = activeChar

            val acted = Formulas.calcMagicAffected(activeChar, target, skill)
            if (target.isInvul || !acted)
                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.MISSED_TARGET))
            else {
                if (skill.hasEffects()) {
                    val shld = Formulas.calcShldUse(activeChar, target, skill)
                    target.stopSkillEffects(skill.id)

                    if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps))
                        skill.getEffects(activeChar, target, Env(shld, sps, false, bsps))
                    else
                        activeChar.sendPacket(
                            SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(
                                target
                            ).addSkillName(skill)
                        )
                }

                val damage = Formulas.calcManaDam(activeChar, target, skill, sps, bsps)

                val mp = if (damage > target.currentMp) target.currentMp else damage
                target.reduceCurrentMp(mp)
                if (damage > 0)
                    target.stopEffectsOnDamage(true)

                if (target is Player) {
                    val sump = StatusUpdate(target)
                    sump.addAttribute(StatusUpdate.CUR_MP, target.getCurrentMp().toInt())
                    target.sendPacket(sump)

                    target.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.S2_MP_HAS_BEEN_DRAINED_BY_S1).addCharName(
                            activeChar
                        ).addNumber(mp.toInt())
                    )
                }

                if (activeChar is Player)
                    activeChar.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.YOUR_OPPONENTS_MP_WAS_REDUCED_BY_S1).addNumber(
                            mp.toInt()
                        )
                    )
            }
        }

        if (skill.hasSelfEffects()) {
            val effect = activeChar.getFirstEffect(skill.id)
            if (effect != null && effect.isSelfEffect)
                effect.exit()

            skill.getEffectsSelf(activeChar)
        }
        activeChar.setChargedShot(if (bsps) ShotType.BLESSED_SPIRITSHOT else ShotType.SPIRITSHOT, skill.isStaticReuse)
    }

    companion object {
        private val SKILL_IDS = arrayOf(L2SkillType.MANADAM)
    }
}