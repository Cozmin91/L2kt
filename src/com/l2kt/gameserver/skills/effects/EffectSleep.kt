package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectFlag
import com.l2kt.gameserver.templates.skills.L2EffectType

internal class EffectSleep(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.SLEEP
    }

    override fun onStart(): Boolean {
        effected.startSleeping()
        return true
    }

    override fun onExit() {
        effected.stopSleeping(false)
    }

    override fun onActionTime(): Boolean {
        return false
    }

    override fun onSameEffect(effect: L2Effect): Boolean {
        return false
    }

    override fun getEffectFlags(): Int {
        return L2EffectFlag.SLEEP.mask
    }
}