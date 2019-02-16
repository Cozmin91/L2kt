package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.ShotType
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.skills.Formulas
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.templates.skills.L2SkillType

class StrSiegeAssault : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        if (activeChar !is Player)
            return

        if (!activeChar.checkIfOkToUseStriderSiegeAssault(skill))
            return

        var damage = 0

        val ss = activeChar.isChargedShot(ShotType.SOULSHOT)

        for (obj in targets) {
            if (obj !is Creature)
                continue

            if (obj.isAlikeDead)
                continue

            val shld = Formulas.calcShldUse(activeChar, obj, null)
            val crit = Formulas.calcCrit(activeChar.getCriticalHit(obj, skill).toDouble())

            if (!crit && skill.condition and L2Skill.COND_CRIT != 0)
                damage = 0
            else
                damage = Formulas.calcPhysDam(activeChar, obj, skill, shld, crit, ss).toInt()

            if (damage > 0) {
                activeChar.sendDamageMessage(obj, damage, false, false, false)
                obj.reduceCurrentHp(damage.toDouble(), activeChar, skill)
            } else
                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ATTACK_FAILED))
        }
        activeChar.setChargedShot(ShotType.SOULSHOT, skill.isStaticReuse)
    }

    companion object {
        private val SKILL_IDS = arrayOf(L2SkillType.STRSIEGEASSAULT)
    }
}