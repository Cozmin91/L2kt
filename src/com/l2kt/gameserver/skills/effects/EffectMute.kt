package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectFlag
import com.l2kt.gameserver.templates.skills.L2EffectType

class EffectMute(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.MUTE
    }

    override fun onStart(): Boolean {
        effected.startMuted()
        return true
    }

    override fun onActionTime(): Boolean {
        return false
    }

    override fun onExit() {
        effected.stopMuted(false)
    }

    override fun getEffectFlags(): Int {
        return L2EffectFlag.MUTED.mask
    }
}