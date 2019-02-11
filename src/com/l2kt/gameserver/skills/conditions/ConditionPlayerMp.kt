package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.skills.Env

class ConditionPlayerMp(private val _mp: Int) : Condition() {

    override fun testImpl(env: Env): Boolean {
        return env.character!!.currentMp * 100 / env.character!!.maxMp <= _mp
    }
}