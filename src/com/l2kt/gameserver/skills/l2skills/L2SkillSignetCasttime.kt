package com.l2kt.gameserver.skills.l2skills

import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.templates.StatsSet

class L2SkillSignetCasttime(set: StatsSet) : L2Skill(set) {
    var _effectNpcId: Int = 0
    var signetEffectId: Int = 0

    init {
        _effectNpcId = set.getInteger("effectNpcId", -1)
        signetEffectId = set.getInteger("effectId", -1)
    }

    override fun useSkill(caster: Creature, targets: Array<WorldObject>) {
        if (caster.isAlikeDead)
            return

        getEffectsSelf(caster)
    }
}