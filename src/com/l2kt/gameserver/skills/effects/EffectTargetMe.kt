package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectType

class EffectTargetMe(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.TARGET_ME
    }

    override fun onStart(): Boolean {
        if (effected is Player) {
            if (effected.target === effector)
                effected.ai.setIntention(CtrlIntention.ATTACK, effector)
            else
                effected.target = effector

            return true
        }
        return false
    }

    override fun onExit() {}

    override fun onActionTime(): Boolean {
        return false
    }
}