package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Stats

class ConditionSkillStats(private val _stat: Stats) : Condition() {

    override fun testImpl(env: Env): Boolean {
        return env.skill != null && env.skill!!.stat == _stat
    }
}