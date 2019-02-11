package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectType

class EffectInvincible(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.INVINCIBLE
    }

    override fun onStart(): Boolean {
        effected.setIsInvul(true)
        return super.onStart()
    }

    override fun onActionTime(): Boolean {
        return false
    }

    override fun onExit() {
        effected.setIsInvul(false)
        super.onExit()
    }
}