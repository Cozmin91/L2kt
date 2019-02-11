package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.actor.Summon
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectFlag
import com.l2kt.gameserver.templates.skills.L2EffectType

internal class EffectBetray(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.BETRAY
    }

    override fun onStart(): Boolean {
        if (effector is Player && effected is Summon) {
            val targetOwner = effected.actingPlayer
            effected.ai.setIntention(CtrlIntention.ATTACK, targetOwner)
            return true
        }
        return false
    }

    override fun onExit() {
        effected.ai.setIntention(CtrlIntention.IDLE)
    }

    override fun onActionTime(): Boolean {
        return false
    }

    override fun getEffectFlags(): Int {
        return L2EffectFlag.BETRAYED.mask
    }
}