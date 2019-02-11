package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.effects.EffectSeed

class ConditionElementSeed(private val _requiredSeeds: IntArray) : Condition() {

    override fun testImpl(env: Env): Boolean {
        val Seeds = IntArray(3)
        for (i in Seeds.indices) {
            Seeds[i] = if (env.character?.getFirstEffect(SEED_SKILLS[i]) is EffectSeed) (env.character?.getFirstEffect(
                SEED_SKILLS[i]
            ) as EffectSeed).power else 0
            if (Seeds[i] >= _requiredSeeds[i])
                Seeds[i] -= _requiredSeeds[i]
            else
                return false
        }

        if (_requiredSeeds[3] > 0) {
            var count = 0
            var i = 0
            while (i < Seeds.size && count < _requiredSeeds[3]) {
                if (Seeds[i] > 0) {
                    Seeds[i]--
                    count++
                }
                i++
            }
            if (count < _requiredSeeds[3])
                return false
        }

        if (_requiredSeeds[4] > 0) {
            var count = 0
            var i = 0
            while (i < Seeds.size && count < _requiredSeeds[4]) {
                count += Seeds[i]
                i++
            }
            if (count < _requiredSeeds[4])
                return false
        }

        return true
    }

    companion object {
        private val SEED_SKILLS = intArrayOf(1285, 1286, 1287)
    }
}