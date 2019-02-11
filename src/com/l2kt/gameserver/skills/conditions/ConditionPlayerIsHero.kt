package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.skills.Env

class ConditionPlayerIsHero
    (private val _val: Boolean) : Condition() {

    override fun testImpl(env: Env): Boolean {
        return if (env.player == null) false else env.player!!.isHero == _val

    }
}