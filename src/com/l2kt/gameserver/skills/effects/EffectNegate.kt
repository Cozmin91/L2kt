package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectType

class EffectNegate(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.NEGATE
    }

    override fun onStart(): Boolean {
        val skill = skill

        for (negateSkillId in skill.negateId) {
            if (negateSkillId != 0)
                effected.stopSkillEffects(negateSkillId)
        }
        for (negateSkillType in skill.negateStats) {
            effected.stopSkillEffects(negateSkillType, skill.negateLvl)
        }
        return true
    }

    override fun onActionTime(): Boolean {
        return false
    }
}