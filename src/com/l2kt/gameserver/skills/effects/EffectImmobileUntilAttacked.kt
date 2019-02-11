package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectFlag
import com.l2kt.gameserver.templates.skills.L2EffectType

class EffectImmobileUntilAttacked(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.IMMOBILEUNTILATTACKED
    }

    override fun onStart(): Boolean {
        effected.startImmobileUntilAttacked()
        return true
    }

    override fun onExit() {
        effected.stopImmobileUntilAttacked(this)
    }

    override fun onActionTime(): Boolean {
        effected.stopImmobileUntilAttacked(this)
        return false
    }

    override fun getEffectFlags(): Int {
        return L2EffectFlag.MEDITATING.mask
    }
}