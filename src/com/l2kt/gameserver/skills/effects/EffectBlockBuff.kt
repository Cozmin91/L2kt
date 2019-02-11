package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectType

class EffectBlockBuff(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.BLOCK_BUFF
    }

    override fun onStart(): Boolean {
        return true
    }

    override fun onActionTime(): Boolean {
        return false
    }
}