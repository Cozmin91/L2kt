package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectFlag
import com.l2kt.gameserver.templates.skills.L2EffectType

class EffectSilenceMagicPhysical(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.SILENCE_MAGIC_PHYSICAL
    }

    override fun onStart(): Boolean {
        effected.startMuted()
        effected.startPhysicalMuted()
        return true
    }

    override fun onActionTime(): Boolean {
        return false
    }

    override fun onExit() {
        effected.stopMuted(false)
        effected.stopPhysicalMuted(false)
    }

    override fun getEffectFlags(): Int {
        return L2EffectFlag.MUTED.mask or L2EffectFlag.PHYSICAL_MUTED.mask
    }
}