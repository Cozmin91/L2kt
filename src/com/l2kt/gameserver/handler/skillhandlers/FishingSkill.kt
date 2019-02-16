package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.ShotType
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.type.WeaponType
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.templates.skills.L2SkillType

class FishingSkill : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        if (activeChar !is Player)
            return

        val isReelingSkill = skill.skillType === L2SkillType.REELING

        if (!activeChar.fishingStance.isUnderFishCombat) {
            activeChar.sendPacket(if (isReelingSkill) SystemMessageId.CAN_USE_REELING_ONLY_WHILE_FISHING else SystemMessageId.CAN_USE_PUMPING_ONLY_WHILE_FISHING)
            activeChar.sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        val fishingRod = activeChar.getActiveWeaponInstance()
        if (fishingRod == null || fishingRod.item.itemType !== WeaponType.FISHINGROD)
            return

        val ssBonus = if (activeChar.isChargedShot(ShotType.FISH_SOULSHOT)) 2 else 1
        val gradeBonus = 1 + (fishingRod.item.crystalType?.id ?: 0) * 0.1

        var damage = (skill.power * gradeBonus * ssBonus.toDouble()).toInt()
        var penalty = 0

        // Fish expertise penalty if skill level is superior or equal to 3.
        if (skill.level - activeChar.getSkillLevel(1315) >= 3) {
            penalty = 50
            damage -= penalty

            activeChar.sendPacket(SystemMessageId.REELING_PUMPING_3_LEVELS_HIGHER_THAN_FISHING_PENALTY)
        }

        if (ssBonus > 1)
            fishingRod.setChargedShot(ShotType.FISH_SOULSHOT, false)

        if (isReelingSkill)
            activeChar.fishingStance.useRealing(damage, penalty)
        else
            activeChar.fishingStance.usePomping(damage, penalty)
    }

    companion object {
        private val SKILL_IDS = arrayOf(L2SkillType.PUMPING, L2SkillType.REELING)
    }
}