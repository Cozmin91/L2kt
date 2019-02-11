package com.l2kt.gameserver.skills.conditions

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.skills.Env

class ConditionGameChance(private val _chance: Int) : Condition() {

    override fun testImpl(env: Env): Boolean {
        return Rnd[100] < _chance
    }
}
