package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.skills.AbnormalEffect
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectType

class EffectGrow(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.BUFF
    }

    override fun onStart(): Boolean {
        if (effected is Npc) {
            val npc = effected as Npc
            npc.collisionRadius = npc.collisionRadius * 1.19

            effected.startAbnormalEffect(AbnormalEffect.GROW)
            return true
        }
        return false
    }

    override fun onActionTime(): Boolean {
        return false
    }

    override fun onExit() {
        if (effected is Npc) {
            val npc = effected as Npc
            npc.collisionRadius = npc.template.collisionRadius

            effected.stopAbnormalEffect(AbnormalEffect.GROW)
        }
    }
}