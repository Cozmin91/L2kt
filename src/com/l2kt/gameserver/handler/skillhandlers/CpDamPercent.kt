package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.ShotType
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Formulas
import com.l2kt.gameserver.templates.skills.L2SkillType

class CpDamPercent : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        if (activeChar.isAlikeDead)
            return

        val ss = activeChar.isChargedShot(ShotType.SOULSHOT)
        val sps = activeChar.isChargedShot(ShotType.SPIRITSHOT)
        val bsps = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT)

        for (obj in targets) {
            if (obj !is Creature)
                continue

            if (activeChar is Player && obj is Player && obj.isFakeDeath)
                obj.stopFakeDeath(true)
            else if (obj.isDead() || obj.isInvul)
                continue

            val shld = Formulas.calcShldUse(activeChar, obj, skill)

            val damage = (obj.currentCp * (skill.power / 100)).toInt()

            // Manage cast break of the target (calculating rate, sending message...)
            Formulas.calcCastBreak(obj, damage.toDouble())

            skill.getEffects(activeChar, obj, Env(shld, ss, sps, bsps))
            activeChar.sendDamageMessage(obj, damage, false, false, false)
            obj.currentCp = obj.currentCp - damage

            // Custom message to see Wrath damage on target
            obj.sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.S1_GAVE_YOU_S2_DMG).addCharName(activeChar).addNumber(
                    damage
                )
            )
        }
        activeChar.setChargedShot(ShotType.SOULSHOT, skill.isStaticReuse)
    }

    companion object {
        private val SKILL_IDS = arrayOf(L2SkillType.CPDAMPERCENT)
    }
}