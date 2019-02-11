package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.skills.Env

class ConditionTargetHpMinMax(private val _minHp: Int, private val _maxHp: Int) : Condition() {

    override fun testImpl(env: Env): Boolean {
        if (env.target == null)
            return false

        val currentHp = env.target!!.currentHp.toInt() * 100 / env.target!!.maxHp
        return currentHp in _minHp.._maxHp
    }
}