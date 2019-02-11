package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.skills.Env

/**
 * The Class ConditionPlayerActiveEffectId.
 */
class ConditionPlayerActiveEffectId : Condition {
    private val _effectId: Int
    private val _effectLvl: Int

    constructor(effectId: Int) {
        _effectId = effectId
        _effectLvl = -1
    }

    constructor(effectId: Int, effectLevel: Int) {
        _effectId = effectId
        _effectLvl = effectLevel
    }

    override fun testImpl(env: Env): Boolean {
        val e = env.character?.getFirstEffect(_effectId)
        return e != null && (_effectLvl == -1 || _effectLvl <= e.skill.level)

    }
}