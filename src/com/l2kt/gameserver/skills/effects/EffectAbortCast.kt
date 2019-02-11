package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectType

class EffectAbortCast(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.ABORT_CAST
    }

    override fun onStart(): Boolean {
        if (effected == null || effected === effector || effected.isRaidRelated)
            return false

        effected.breakCast()
        return true
    }

    override fun onActionTime(): Boolean {
        return false
    }
}