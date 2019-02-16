package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.templates.skills.L2SkillType

class TakeCastle : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        if (activeChar == null || activeChar !is Player)
            return

        if (targets.size == 0)
            return

        if (!activeChar.isClanLeader)
            return

        val castle = CastleManager.getCastle(activeChar)
        if (castle == null || !activeChar.checkIfOkToCastSealOfRule(castle, true, skill, targets[0]))
            return

        castle.engrave(activeChar.clan, targets[0])
    }

    companion object {
        private val SKILL_IDS = arrayOf(L2SkillType.TAKECASTLE)
    }
}