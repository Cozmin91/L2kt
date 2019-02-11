package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.effects.EffectFusion

class ConditionForceBuff
    (private val _forces: ByteArray) : Condition() {
    override fun testImpl(env: Env): Boolean {
        if (_forces[0] > 0) {
            val force = env.character?.getFirstEffect(BATTLE_FORCE.toInt())
            if (force == null || (force as EffectFusion).effect < _forces[0])
                return false
        }

        if (_forces[1] > 0) {
            val force = env.character?.getFirstEffect(SPELL_FORCE.toInt())
            if (force == null || (force as EffectFusion).effect < _forces[1])
                return false
        }
        return true
    }

    companion object {
        private const val BATTLE_FORCE: Short = 5104
        private const val SPELL_FORCE: Short = 5105
    }
}