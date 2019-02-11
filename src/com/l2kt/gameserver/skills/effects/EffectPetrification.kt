package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.skills.AbnormalEffect
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectFlag
import com.l2kt.gameserver.templates.skills.L2EffectType

class EffectPetrification(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.PETRIFICATION
    }

    override fun onStart(): Boolean {
        effected.startAbnormalEffect(AbnormalEffect.HOLD_2)
        effected.startParalyze()
        effected.setIsInvul(true)
        return true
    }

    override fun onExit() {
        effected.stopAbnormalEffect(AbnormalEffect.HOLD_2)
        effected.stopParalyze(false)
        effected.setIsInvul(false)
    }

    override fun onActionTime(): Boolean {
        return false
    }

    override fun getEffectFlags(): Int {
        return L2EffectFlag.PARALYZED.mask
    }
}