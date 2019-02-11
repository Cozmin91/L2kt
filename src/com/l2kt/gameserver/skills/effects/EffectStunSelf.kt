package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectFlag
import com.l2kt.gameserver.templates.skills.L2EffectType

class EffectStunSelf(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.STUN_SELF
    }

    override fun onStart(): Boolean {
        effector.startStunning()
        return true
    }

    override fun onExit() {
        effector.stopStunning(false)
    }

    override fun onActionTime(): Boolean {
        return false
    }

    override fun isSelfEffectType(): Boolean {
        return true
    }

    override fun getEffectFlags(): Int {
        return L2EffectFlag.STUNNED.mask
    }
}