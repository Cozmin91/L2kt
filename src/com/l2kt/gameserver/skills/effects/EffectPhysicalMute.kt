package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectFlag
import com.l2kt.gameserver.templates.skills.L2EffectType

class EffectPhysicalMute(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.PHYSICAL_MUTE
    }

    override fun onStart(): Boolean {
        effected.startPhysicalMuted()
        return true
    }

    override fun onActionTime(): Boolean {
        return false
    }

    override fun onExit() {
        effected.stopPhysicalMuted(false)
    }

    override fun getEffectFlags(): Int {
        return L2EffectFlag.PHYSICAL_MUTED.mask
    }
}