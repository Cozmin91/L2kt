package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectType

class EffectIncreaseCharges(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.INCREASE_CHARGES
    }

    override fun onStart(): Boolean {
        if (effected == null)
            return false

        if (effected !is Player)
            return false

        (effected as Player).increaseCharges(calc().toInt(), count)
        return true
    }

    override fun onActionTime(): Boolean {
        return false
    }
}