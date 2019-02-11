package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectType

internal class EffectImobileBuff(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.BUFF
    }

    override fun onStart(): Boolean {
        effector.setIsImmobilized(true)
        return true
    }

    override fun onExit() {
        effector.setIsImmobilized(false)
    }

    override fun onActionTime(): Boolean {
        return false
    }
}