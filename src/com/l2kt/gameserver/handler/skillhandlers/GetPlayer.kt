package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.network.serverpackets.FlyToLocation
import com.l2kt.gameserver.network.serverpackets.FlyToLocation.FlyType
import com.l2kt.gameserver.network.serverpackets.ValidateLocation
import com.l2kt.gameserver.templates.skills.L2SkillType

class GetPlayer : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        if (activeChar.isAlikeDead)
            return

        for (target in targets) {
            val victim = target.actingPlayer
            if (victim == null || victim.isAlikeDead)
                continue

            // Cancel current actions.
            victim.stopMove(null)
            victim.abortAttack()
            victim.abortCast()

            // Teleport the actor.
            victim.broadcastPacket(FlyToLocation(victim, activeChar, FlyType.DUMMY))
            victim.setXYZ(activeChar.x, activeChar.y, activeChar.z)
            victim.broadcastPacket(ValidateLocation(victim))
        }
    }

    companion object {
        private val SKILL_IDS = arrayOf(L2SkillType.GET_PLAYER)
    }
}