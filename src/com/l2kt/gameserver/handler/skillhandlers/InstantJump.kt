package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.commons.math.MathUtil
import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature

import com.l2kt.gameserver.network.serverpackets.ValidateLocation
import com.l2kt.gameserver.templates.skills.L2SkillType

class InstantJump : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        val target = targets[0] as Creature

        val px = target.x
        val py = target.y
        var ph = MathUtil.convertHeadingToDegree(target.heading)

        ph += 180.0

        if (ph > 360)
            ph -= 360.0

        ph = Math.PI * ph / 180

        val x = (px + 25 * Math.cos(ph)).toInt()
        val y = (py + 25 * Math.sin(ph)).toInt()
        val z = target.z

        // Cancel current actions.
        activeChar.stopMove(null)
        activeChar.abortAttack()
        activeChar.abortCast()

        // Teleport the actor.
        activeChar.setXYZ(x, y, z)
        activeChar.broadcastPacket(ValidateLocation(activeChar))
    }

    companion object {
        private val SKILL_IDS = arrayOf(L2SkillType.INSTANT_JUMP)
    }
}