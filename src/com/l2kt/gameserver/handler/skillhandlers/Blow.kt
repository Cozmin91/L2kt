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
import com.l2kt.gameserver.templates.skills.L2SkillType

class Blow : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        if (activeChar.isAlikeDead)
            return

        val ss = activeChar.isChargedShot(ShotType.SOULSHOT)

        for (obj in targets) {
            if (obj !is Creature)
                continue

            if (obj.isAlikeDead)
                continue

            var _successChance = SIDE.toByte()

            if (activeChar.isBehindTarget)
                _successChance = BEHIND.toByte()
            else if (activeChar.isInFrontOfTarget)
                _successChance = FRONT.toByte()

            // If skill requires Crit or skill requires behind, calculate chance based on DEX, Position and on self BUFF
            var success = true
            if (skill.condition and L2Skill.COND_BEHIND != 0)
                success = _successChance.toInt() == BEHIND
            if (skill.condition and L2Skill.COND_CRIT != 0)
                success = success && Formulas.calcBlow(activeChar, obj, _successChance.toInt())

            if (success) {
                // Calculate skill evasion
                val skillIsEvaded = Formulas.calcPhysicalSkillEvasion(obj, skill)
                if (skillIsEvaded) {
                    if (activeChar is Player)
                        activeChar.sendPacket(
                            SystemMessage.getSystemMessage(SystemMessageId.S1_DODGES_ATTACK).addCharName(
                                obj
                            )
                        )

                    if (obj is Player)
                        obj.sendPacket(
                            SystemMessage.getSystemMessage(SystemMessageId.AVOIDED_S1_ATTACK).addCharName(
                                activeChar
                            )
                        )

                    // no futher calculations needed.
                    continue
                }

                // Calculate skill reflect
                val reflect = Formulas.calcSkillReflect(obj, skill)
                if (skill.hasEffects()) {
                    if (reflect == Formulas.SKILL_REFLECT_SUCCEED) {
                        activeChar.stopSkillEffects(skill.id)
                        skill.getEffects(obj, activeChar)
                        activeChar.sendPacket(
                            SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(
                                skill
                            )
                        )
                    } else {
                        val shld = Formulas.calcShldUse(activeChar, obj, skill)
                        obj.stopSkillEffects(skill.id)
                        if (Formulas.calcSkillSuccess(activeChar, obj, skill, shld, true)) {
                            skill.getEffects(activeChar, obj, Env(shld, false, false, false))
                            obj.sendPacket(
                                SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(
                                    skill
                                )
                            )
                        } else
                            activeChar.sendPacket(
                                SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(
                                    obj
                                ).addSkillName(skill)
                            )
                    }
                }

                val shld = Formulas.calcShldUse(activeChar, obj, skill)

                // Crit rate base crit rate for skill, modified with STR bonus
                var crit = false
                if (Formulas.calcCrit(skill.baseCritRate.toDouble() * 10.0 * Formulas.getSTRBonus(activeChar)))
                    crit = true

                var damage = Formulas.calcBlowDamage(activeChar, obj, skill, shld, ss).toInt().toDouble()
                if (crit) {
                    damage *= 2.0

                    // Vicious Stance is special after C5, and only for BLOW skills
                    val vicious = activeChar.getFirstEffect(312)
                    if (vicious != null && damage > 1) {
                        for (func in vicious.statFuncs) {
                            val env = Env()
                            env.character = activeChar
                            env.target = obj
                            env.skill = skill
                            env.value = damage

                            func.calc(env)
                            damage = env.value.toInt().toDouble()
                        }
                    }
                }

                obj.reduceCurrentHp(damage, activeChar, skill)

                // vengeance reflected damage
                if ((reflect.toInt() and Formulas.SKILL_REFLECT_VENGEANCE.toInt()) != 0) {
                    if (obj is Player)
                        obj.sendPacket(
                            SystemMessage.getSystemMessage(SystemMessageId.COUNTERED_S1_ATTACK).addCharName(
                                activeChar
                            )
                        )

                    if (activeChar is Player)
                        activeChar.sendPacket(
                            SystemMessage.getSystemMessage(SystemMessageId.S1_PERFORMING_COUNTERATTACK).addCharName(
                                obj
                            )
                        )

                    // Formula from Diego post, 700 from rpg tests
                    val vegdamage = (700 * obj.getPAtk(activeChar) / activeChar.getPDef(obj)).toDouble()
                    activeChar.reduceCurrentHp(vegdamage, obj, skill)
                }

                // Manage cast break of the target (calculating rate, sending message...)
                Formulas.calcCastBreak(obj, damage)

                if (activeChar is Player)
                    activeChar.sendDamageMessage(obj, damage.toInt(), false, true, false)
            } else
                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ATTACK_FAILED))

            // Possibility of a lethal strike
            Formulas.calcLethalHit(activeChar, obj, skill)

            if (skill.hasSelfEffects()) {
                val effect = activeChar.getFirstEffect(skill.id)
                if (effect != null && effect.isSelfEffect)
                    effect.exit()

                skill.getEffectsSelf(activeChar)
            }
            activeChar.setChargedShot(ShotType.SOULSHOT, skill.isStaticReuse)
        }
    }

    companion object {
        private val SKILL_IDS = arrayOf(L2SkillType.BLOW)

        val FRONT = 50
        val SIDE = 60
        val BEHIND = 70
    }
}