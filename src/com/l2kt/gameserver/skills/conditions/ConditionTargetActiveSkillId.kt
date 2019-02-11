package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.skills.Env

class ConditionTargetActiveSkillId
    (private val _skillId: Int) : Condition() {

    override fun testImpl(env: Env): Boolean {
        return env.target!!.getSkill(_skillId) != null
    }
}