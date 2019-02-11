package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.skills.Env

class ConditionPlayerCharges
    (private val _charges: Int) : Condition() {

    override fun testImpl(env: Env): Boolean {
        return env.player != null && env.player!!.charges >= _charges
    }
}