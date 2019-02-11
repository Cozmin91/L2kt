package com.l2kt.gameserver.skills.l2skills

import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.templates.StatsSet

class L2SkillDefault(set: StatsSet) : L2Skill(set) {

    override fun useSkill(caster: Creature, targets: Array<WorldObject>) {
        caster.sendPacket(ActionFailed.STATIC_PACKET)
        caster.sendMessage("Skill $id [$skillType] isn't implemented.")
    }
}