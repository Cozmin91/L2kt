package com.l2kt.gameserver.skills.l2skills

import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.ShotType
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Formulas
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.templates.StatsSet

class L2SkillChargeDmg(set: StatsSet) : L2Skill(set) {

    override fun useSkill(caster: Creature, targets: Array<WorldObject>) {
        if (caster.isAlikeDead)
            return

        var modifier = 0.0

        if (caster is Player)
            modifier = 0.7 + 0.3 * (caster.charges + numCharges)

        val ss = caster.isChargedShot(ShotType.SOULSHOT)

        for (obj in targets) {
            if (obj !is Creature)
                continue

            if (obj.isAlikeDead)
                continue

            // Calculate skill evasion
            val skillIsEvaded = Formulas.calcPhysicalSkillEvasion(obj, this)
            if (skillIsEvaded) {
                if (caster is Player)
                    caster.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DODGES_ATTACK).addCharName(obj))

                if (obj is Player)
                    obj.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AVOIDED_S1_ATTACK).addCharName(caster))

                // no futher calculations needed.
                continue
            }

            val shld = Formulas.calcShldUse(caster, obj, this)
            var crit = false

            if (baseCritRate > 0)
                crit = Formulas.calcCrit(baseCritRate.toDouble() * 10.0 * Formulas.getSTRBonus(caster))

            // damage calculation, crit is static 2x
            var damage = Formulas.calcPhysDam(caster, obj, this, shld, false, ss)
            if (crit)
                damage *= 2.0

            if (damage > 0) {
                val reflect = Formulas.calcSkillReflect(obj, this)
                if (hasEffects()) {
                    if ((reflect.toInt() and Formulas.SKILL_REFLECT_SUCCEED.toInt()) != 0) {
                        caster.stopSkillEffects(id)
                        getEffects(obj, caster)
                        caster.sendPacket(
                            SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(
                                this
                            )
                        )
                    } else {
                        // activate attacked effects, if any
                        obj.stopSkillEffects(id)
                        if (Formulas.calcSkillSuccess(caster, obj, this, shld, true)) {
                            getEffects(caster, obj, Env(shld, false, false, false))
                            obj.sendPacket(
                                SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(
                                    this
                                )
                            )
                        } else
                            caster.sendPacket(
                                SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(
                                    obj
                                ).addSkillName(this)
                            )
                    }
                }

                val finalDamage = damage * modifier
                obj.reduceCurrentHp(finalDamage, caster, this)

                // vengeance reflected damage
                if ((reflect.toInt() and Formulas.SKILL_REFLECT_VENGEANCE.toInt()) != 0)
                    caster.reduceCurrentHp(damage, obj, this)

                caster.sendDamageMessage(obj, finalDamage.toInt(), false, crit, false)
            } else
                caster.sendDamageMessage(obj, 0, false, false, true)
        }

        if (hasSelfEffects()) {
            val effect = caster.getFirstEffect(id)
            if (effect != null && effect.isSelfEffect)
                effect.exit()

            getEffectsSelf(caster)
        }

        caster.setChargedShot(ShotType.SOULSHOT, isStaticReuse)
    }
}