package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectFlag
import com.l2kt.gameserver.templates.skills.L2EffectType

class EffectProtectionBlessing(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.PROTECTION_BLESSING
    }

    override fun onStart(): Boolean {
        return false
    }

    override fun onExit() {
        (effected as Playable).stopProtectionBlessing(this)
    }

    override fun onActionTime(): Boolean {
        return false
    }

    override fun getEffectFlags(): Int {
        return L2EffectFlag.PROTECTION_BLESSING.mask
    }
}