package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.skills.Env

class ConditionPlayerLevel(private val _level: Int) : Condition() {

    override fun testImpl(env: Env): Boolean {
        return env.character!!.level >= _level
    }
}