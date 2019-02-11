package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.skills.Env

class ConditionPlayerHp(private val _hp: Int) : Condition() {

    override fun testImpl(env: Env): Boolean {
        return env.character!!.currentHp * 100 / env.character!!.maxHp <= _hp
    }
}