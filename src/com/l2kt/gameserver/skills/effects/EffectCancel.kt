package com.l2kt.gameserver.skills.effects

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Formulas

import com.l2kt.gameserver.templates.skills.L2EffectType

/**
 * @author DS
 */
class EffectCancel(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.CANCEL
    }

    override fun onStart(): Boolean {
        return cancel(effector, effected, this)
    }

    override fun onActionTime(): Boolean {
        return false
    }

    private fun cancel(caster: Creature, target: Creature, effect: L2Effect): Boolean {
        if (target !is Player || target.isDead())
            return false

        val cancelLvl = effect.skill.magicLevel
        var count = effect.skill.maxNegatedEffects

        var rate = effect.effectPower

        // Resistance/vulnerability
        val res = Formulas.calcSkillVulnerability(caster, target, effect.skill, effect.skillType)
        rate *= res

        var eff: L2Effect
        var lastCanceledSkillId = 0
        val effects = target.getAllEffects().filterNotNull()
        run {
            var i = effects.size
            while (--i >= 0) {
                eff = effects[i]

                // first pass - dances/songs only
                if (!eff.skill.isDance)
                    continue

                if (eff.skill.id == lastCanceledSkillId) {
                    eff.exit() // this skill already canceled
                    continue
                }

                if (!calcCancelSuccess(eff, cancelLvl, rate.toInt()))
                    continue

                lastCanceledSkillId = eff.skill.id
                eff.exit()
                count--

                if (count == 0)
                    break
            }
        }

        if (count != 0) {
            lastCanceledSkillId = 0
            var i = effects.size
            while (--i >= 0) {
                eff = effects[i]

                // second pass - all except dances/songs
                if (eff.skill.isDance)
                    continue

                if (eff.skill.id == lastCanceledSkillId) {
                    eff.exit() // this skill already canceled
                    continue
                }

                if (!calcCancelSuccess(eff, cancelLvl, rate.toInt()))
                    continue

                lastCanceledSkillId = eff.skill.id
                eff.exit()
                count--

                if (count == 0)
                    break
            }
        }
        return true
    }

    private fun calcCancelSuccess(effect: L2Effect, cancelLvl: Int, baseRate: Int): Boolean {
        var rate = 2 * (cancelLvl - effect.skill.magicLevel)
        rate += effect.period / 120
        rate += baseRate

        if (rate < 25)
            rate = 25
        else if (rate > 75)
            rate = 75

        return Rnd[100] < rate
    }
}