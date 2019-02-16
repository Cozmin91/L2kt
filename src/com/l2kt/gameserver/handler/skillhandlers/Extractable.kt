package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.templates.skills.L2SkillType

class Extractable : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        if (activeChar !is Player)
            return

        val exItem = skill.extractableSkill
        if (exItem == null || exItem.productItemsArray.isEmpty()) {
            ISkillHandler.Companion._log.warning("Missing informations for extractable skill id: " + skill.id + ".")
            return
        }

        val player = activeChar.actingPlayer
        val chance = Rnd[100000]

        var created = false
        var chanceIndex = 0

        for ((items, chance1) in exItem.productItemsArray) {
            chanceIndex += (chance1 * 1000).toInt()
            if (chance <= chanceIndex) {
                for (item in items)
                    player!!.addItem("Extract", item.id, item.value, targets[0], true)

                created = true
                break
            }
        }

        if (!created) {
            player!!.sendPacket(SystemMessageId.NOTHING_INSIDE_THAT)
            return
        }
    }

    companion object {
        private val SKILL_IDS = arrayOf(L2SkillType.EXTRACTABLE, L2SkillType.EXTRACTABLE_FISH)
    }
}