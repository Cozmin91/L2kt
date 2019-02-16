package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.handler.SkillHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.StatusUpdate
import com.l2kt.gameserver.templates.skills.L2SkillType
import java.util.*

class BalanceLife : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        val handler = SkillHandler.getHandler(L2SkillType.BUFF)
        handler?.useSkill(activeChar, skill, targets)

        val player = activeChar.actingPlayer
        val finalList = ArrayList<Creature>()

        var fullHP = 0.0
        var currentHPs = 0.0

        for (obj in targets) {
            if (obj !is Creature)
                continue

            if (obj.isDead)
                continue

            // Player holding a cursed weapon can't be healed and can't heal
            if (obj !== activeChar) {
                if (obj is Player && obj.isCursedWeaponEquipped)
                    continue
                else if (player != null && player.isCursedWeaponEquipped)
                    continue
            }

            fullHP += obj.maxHp.toDouble()
            currentHPs += obj.currentHp

            // Add the character to the final list.
            finalList.add(obj)
        }

        if (!finalList.isEmpty()) {
            val percentHP = currentHPs / fullHP

            for (target in finalList) {
                target.currentHp = target.maxHp * percentHP

                val su = StatusUpdate(target)
                su.addAttribute(StatusUpdate.CUR_HP, target.currentHp.toInt())
                target.sendPacket(su)
            }
        }
    }

    companion object {
        private val SKILL_IDS = arrayOf(L2SkillType.BALANCE_LIFE)
    }
}