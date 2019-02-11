package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.skills.Env

class ConditionPlayerPkCount (private val _pk: Int) : Condition() {

    override fun testImpl(env: Env): Boolean {
        return if (env.player == null) false else env.player!!.pkKills <= _pk

    }
}