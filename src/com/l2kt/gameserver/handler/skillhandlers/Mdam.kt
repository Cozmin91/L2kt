package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.ShotType
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Formulas
import com.l2kt.gameserver.templates.skills.L2EffectType
import com.l2kt.gameserver.templates.skills.L2SkillType

class Mdam : ISkillHandler {

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

            if (activeChar is Player && obj is Player && obj.isFakeDeath)
                obj.stopFakeDeath(true)
            else if (obj.isDead())
                continue

            val mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(obj, skill))
            val shld = Formulas.calcShldUse(activeChar, obj, skill)
            val reflect = Formulas.calcSkillReflect(obj, skill)

            val damage = Formulas.calcMagicDam(activeChar, obj, skill, shld, sps, bsps, mcrit).toInt()
            if (damage > 0) {
                // Manage cast break of the target (calculating rate, sending message...)
                Formulas.calcCastBreak(obj, damage.toDouble())

                // vengeance reflected damage
                if ((reflect.toInt() and Formulas.SKILL_REFLECT_VENGEANCE.toInt()) != 0)
                    activeChar.reduceCurrentHp(damage.toDouble(), obj, skill)
                else {
                    activeChar.sendDamageMessage(obj, damage, mcrit, false, false)
                    obj.reduceCurrentHp(damage.toDouble(), activeChar, skill)
                }

                if (skill.hasEffects() && obj.getFirstEffect(L2EffectType.BLOCK_DEBUFF) == null) {
                    if ((reflect.toInt() and Formulas.SKILL_REFLECT_SUCCEED.toInt()) != 0)
                    // reflect skill effects
                    {
                        activeChar.stopSkillEffects(skill.id)
                        skill.getEffects(obj, activeChar)
                        activeChar.sendPacket(
                            SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(
                                skill
                            )
                        )
                    } else {
                        // activate attacked effects, if any
                        obj.stopSkillEffects(skill.id)
                        if (Formulas.calcSkillSuccess(activeChar, obj, skill, shld, bsps))
                            skill.getEffects(activeChar, obj, Env(shld, sps, false, bsps))
                        else
                            activeChar.sendPacket(
                                SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(
                                    obj
                                ).addSkillName(skill.id)
                            )
                    }
                }
            }
        }

        if (skill.hasSelfEffects()) {
            val effect = activeChar.getFirstEffect(skill.id)
            if (effect != null && effect.isSelfEffect)
                effect.exit()

            skill.getEffectsSelf(activeChar)
        }

        if (skill.isSuicideAttack)
            activeChar.doDie(null)

        activeChar.setChargedShot(if (bsps) ShotType.BLESSED_SPIRITSHOT else ShotType.SPIRITSHOT, skill.isStaticReuse)
    }

    companion object {
        private val SKILL_IDS = arrayOf(L2SkillType.MDAM, L2SkillType.DEATHLINK)
    }
}