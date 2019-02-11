package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.EffectPoint
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectType

class EffectSignetNoise(env: Env, template: EffectTemplate) : L2Effect(env, template) {
    private var _actor: EffectPoint? = null

    override fun getEffectType(): L2EffectType {
        return L2EffectType.SIGNET_GROUND
    }

    override fun onStart(): Boolean {
        _actor = effected as EffectPoint
        return true
    }

    override fun onActionTime(): Boolean {
        if (count == totalCount - 1)
            return true

        val caster = effector as Player

        for (target in _actor!!.getKnownTypeInRadius(Creature::class.java, skill.skillRadius)) {
            if (target === caster)
                continue

            if (caster.canAttackCharacter(target)) {
                for (effect in target.allEffects) {
                    if (effect.skill.isDance)
                        effect.exit()
                }
            }
        }
        return true
    }

    override fun onExit() {
        if (_actor != null)
            _actor!!.deleteMe()
    }
}