package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.skills.Env

class ConditionPlayerWeight
    (private val _weight: Int) : Condition() {

    override fun testImpl(env: Env): Boolean {
        val player = env.player
        if (player != null && player.maxLoad > 0) {
            val weightproc = player.currentLoad * 100 / player.maxLoad
            return weightproc < _weight
        }
        return true
    }
}