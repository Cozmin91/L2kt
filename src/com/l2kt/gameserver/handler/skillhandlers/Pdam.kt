package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.ShotType
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.type.WeaponType
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Formulas
import com.l2kt.gameserver.templates.skills.L2EffectType
import com.l2kt.gameserver.templates.skills.L2SkillType

class Pdam : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        if (activeChar.isAlikeDead)
            return

        var damage = 0

        val ss = activeChar.isChargedShot(ShotType.SOULSHOT)

        val weapon = activeChar.activeWeaponInstance

        for (obj in targets) {
            if (obj !is Creature)
                continue

            if (activeChar is Player && obj is Player && obj.isFakeDeath)
                obj.stopFakeDeath(true)
            else if (obj.isDead())
                continue

            // Calculate skill evasion. As Dodge blocks only melee skills, make an exception with bow weapons.
            if (weapon != null && weapon.itemType !== WeaponType.BOW && Formulas.calcPhysicalSkillEvasion(obj, skill)) {
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

            val shld = Formulas.calcShldUse(activeChar, obj, null)

            // PDAM critical chance not affected by buffs, only by STR. Only some skills are meant to crit.
            var crit = false
            if (skill.baseCritRate > 0)
                crit = Formulas.calcCrit(skill.baseCritRate.toDouble() * 10.0 * Formulas.getSTRBonus(activeChar))

            if (!crit && skill.condition and L2Skill.COND_CRIT != 0)
                damage = 0
            else
                damage = Formulas.calcPhysDam(activeChar, obj, skill, shld, false, ss).toInt()

            if (crit)
                damage *= 2 // PDAM Critical damage always 2x and not affected by buffs

            val reflect = Formulas.calcSkillReflect(obj, skill)

            if (skill.hasEffects() && obj.getFirstEffect(L2EffectType.BLOCK_DEBUFF) == null) {
                val effects: List<L2Effect>?
                if ((reflect.toInt() and Formulas.SKILL_REFLECT_SUCCEED.toInt()) != 0) {
                    activeChar.stopSkillEffects(skill.id)
                    effects = skill.getEffects(obj, activeChar)
                    if (effects != null && !effects.isEmpty())
                        activeChar.sendPacket(
                            SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(
                                skill
                            )
                        )
                } else {
                    // activate attacked effects, if any
                    obj.stopSkillEffects(skill.id)
                    effects = skill.getEffects(activeChar, obj, Env(shld, false, false, false))
                    if (effects != null && !effects.isEmpty())
                        obj.sendPacket(
                            SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(
                                skill
                            )
                        )
                }
            }

            if (damage > 0) {
                activeChar.sendDamageMessage(obj, damage, false, crit, false)

                // Possibility of a lethal strike
                Formulas.calcLethalHit(activeChar, obj, skill)

                obj.reduceCurrentHp(damage.toDouble(), activeChar, skill)

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

                    val vegdamage = (700 * obj.getPAtk(activeChar) / activeChar.getPDef(obj)).toDouble()
                    activeChar.reduceCurrentHp(vegdamage, obj, skill)
                }
            } else
                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ATTACK_FAILED))
        }

        if (skill.hasSelfEffects()) {
            val effect = activeChar.getFirstEffect(skill.id)
            if (effect != null && effect.isSelfEffect)
                effect.exit()

            skill.getEffectsSelf(activeChar)
        }

        if (skill.isSuicideAttack)
            activeChar.doDie(null)

        activeChar.setChargedShot(ShotType.SOULSHOT, skill.isStaticReuse)
    }

    companion object {
        private val SKILL_IDS = arrayOf(L2SkillType.PDAM, L2SkillType.FATAL)
    }
}