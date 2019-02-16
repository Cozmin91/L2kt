package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.RecipeBookItemList
import com.l2kt.gameserver.templates.skills.L2SkillType

class Craft : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        if (activeChar !is Player)
            return

        if (activeChar.isInStoreMode) {
            activeChar.sendPacket(SystemMessageId.CANNOT_CREATED_WHILE_ENGAGED_IN_TRADING)
            return
        }

        activeChar.sendPacket(RecipeBookItemList(activeChar, skill.skillType === L2SkillType.DWARVEN_CRAFT))
    }

    companion object {
        private val SKILL_IDS = arrayOf(L2SkillType.COMMON_CRAFT, L2SkillType.DWARVEN_CRAFT)
    }
}