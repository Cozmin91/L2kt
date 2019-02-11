package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectType

class EffectSeed(env: Env, template: EffectTemplate) : L2Effect(env, template) {
    var power = 1
        private set

    override fun getEffectType(): L2EffectType {
        return L2EffectType.SEED
    }

    override fun onActionTime(): Boolean {
        return false
    }

    fun increasePower() {
        power++
    }
}