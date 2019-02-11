package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.skills.Env

class ConditionPlayerActiveSkillId : Condition {
    private val _skillId: Int
    private val _skillLevel: Int

    constructor(skillId: Int) {
        _skillId = skillId
        _skillLevel = -1
    }

    constructor(skillId: Int, skillLevel: Int) {
        _skillId = skillId
        _skillLevel = skillLevel
    }

    override fun testImpl(env: Env): Boolean {
        val skill = env.character?.getSkill(_skillId)
        return skill != null && _skillLevel <= skill.level
    }
}