package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectType

class EffectFusion(env: Env, template: EffectTemplate) : L2Effect(env, template) {
    var effect: Int = skill.level
    var maxEffect: Int = SkillTable.getMaxLevel(skill.id)

    override fun onActionTime(): Boolean {
        return true
    }

    override fun getEffectType(): L2EffectType {
        return L2EffectType.FUSION
    }

    fun increaseEffect() {
        if (effect < maxEffect) {
            effect++
            updateBuff()
        }
    }

    fun decreaseForce() {
        effect--
        if (effect < 1)
            exit()
        else
            updateBuff()
    }

    private fun updateBuff() {
        exit()
        SkillTable.getInfo(skill.id, effect)?.getEffects(effector, effected)
    }
}