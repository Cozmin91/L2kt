package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.actor.Summon
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectType

internal class EffectImobilePetBuff(env: Env, template: EffectTemplate) : L2Effect(env, template) {
    private var _pet: Summon? = null

    override fun getEffectType(): L2EffectType {
        return L2EffectType.BUFF
    }

    override fun onStart(): Boolean {
        _pet = null

        if (effected is Summon && effector is Player && (effected as Summon).owner == effector) {
            _pet = effected as Summon
            _pet!!.setImmobilized(true)
            return true
        }
        return false
    }

    override fun onExit() {
        _pet!!.setImmobilized(false)
    }

    override fun onActionTime(): Boolean {
        return false
    }
}