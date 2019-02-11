package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.skills.Env

class ConditionPlayerHpPercentage(private val _p: Double) : Condition() {

    override fun testImpl(env: Env): Boolean {
        return env.character!!.currentHp <= env.character!!.maxHp * _p
    }
}
