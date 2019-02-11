package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.skills.AbnormalEffect
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectType

/**
 * @author LBaldi
 */
class EffectBigHead(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.BUFF
    }

    override fun onStart(): Boolean {
        effected.startAbnormalEffect(AbnormalEffect.BIG_HEAD)
        return true
    }

    override fun onExit() {
        effected.stopAbnormalEffect(AbnormalEffect.BIG_HEAD)
    }

    override fun onActionTime(): Boolean {
        return false
    }
}