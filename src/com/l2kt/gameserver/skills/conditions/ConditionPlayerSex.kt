package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.skills.Env

class ConditionPlayerSex
    (private val _sex: Int) : Condition() {

    override fun testImpl(env: Env): Boolean {
        return if (env.player == null) false else env.player!!.appearance.sex.ordinal == _sex
    }
}