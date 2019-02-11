package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectType

class EffectRemoveTarget(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.REMOVE_TARGET
    }

    override fun onStart(): Boolean {
        effected.target = null
        effected.abortAttack()
        effected.abortCast()
        effected.ai.setIntention(CtrlIntention.IDLE, effector)
        return true
    }

    override fun onExit() {}

    override fun onActionTime(): Boolean {
        return false
    }
}