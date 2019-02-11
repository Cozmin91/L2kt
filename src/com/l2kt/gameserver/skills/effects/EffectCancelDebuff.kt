package com.l2kt.gameserver.skills.effects

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Formulas

import com.l2kt.gameserver.templates.skills.L2EffectType
import com.l2kt.gameserver.templates.skills.L2SkillType

/**
 * @author UnAfraid
 */
class EffectCancelDebuff(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.CANCEL_DEBUFF
    }

    override fun onStart(): Boolean {
        return cancel(effector, effected, skill, effectTemplate.effectType)
    }

    override fun onActionTime(): Boolean {
        return false
    }

    private fun cancel(caster: Creature, target: Creature, skill: L2Skill, effectType: L2SkillType?): Boolean {
        if (target !is Player || target.isDead())
            return false

        val cancelLvl = skill.magicLevel
        var count = skill.maxNegatedEffects
        val baseRate = Formulas.calcSkillVulnerability(caster, target, skill, effectType)

        var effect: L2Effect?
        var lastCanceledSkillId = 0
        val effects = target.getAllEffects()
        run {
            var i = effects.size
            while (--i >= 0) {
                effect = effects[i]
                if (effect == null)
                    continue

                if (!effect!!.skill.isDebuff || !effect!!.skill.canBeDispeled()) {
                    effects[i] = null
                    continue
                }

                if (effect!!.skill.id == lastCanceledSkillId) {
                    effect!!.exit() // this skill already canceled
                    continue
                }

                if (!calcCancelSuccess(effect!!, cancelLvl, baseRate.toInt()))
                    continue

                lastCanceledSkillId = effect!!.skill.id
                effect!!.exit()
                count--

                if (count == 0)
                    break
            }
        }

        if (count != 0) {
            lastCanceledSkillId = 0
            var i = effects.size
            while (--i >= 0) {
                effect = effects[i]
                if (effect == null)
                    continue

                if (!effect!!.skill.isDebuff || !effect!!.skill.canBeDispeled()) {
                    effects[i] = null
                    continue
                }

                if (effect!!.skill.id == lastCanceledSkillId) {
                    effect!!.exit() // this skill already canceled
                    continue
                }

                if (!calcCancelSuccess(effect!!, cancelLvl, baseRate.toInt()))
                    continue

                lastCanceledSkillId = effect!!.skill.id
                effect!!.exit()
                count--

                if (count == 0)
                    break
            }
        }
        return true
    }

    private fun calcCancelSuccess(effect: L2Effect, cancelLvl: Int, baseRate: Int): Boolean {
        var rate = 2 * (cancelLvl - effect.skill.magicLevel)
        rate += (effect.period - effect.time) / 1200
        rate *= baseRate

        if (rate < 25)
            rate = 25
        else if (rate > 75)
            rate = 75

        return Rnd[100] < rate
    }
}