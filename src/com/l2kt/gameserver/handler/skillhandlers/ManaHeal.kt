package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.ShotType
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.StatusUpdate
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.skills.Stats
import com.l2kt.gameserver.templates.skills.L2SkillType

class ManaHeal : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        for (obj in targets) {
            if (obj !is Creature)
                continue

            if (obj.isInvul)
                continue

            var mp = skill.power

            if (skill.skillType === L2SkillType.MANAHEAL_PERCENT)
                mp = obj.maxMp * mp / 100.0
            else
                mp = if (skill.skillType === L2SkillType.MANARECHARGE) obj.calcStat(
                    Stats.RECHARGE_MP_RATE,
                    mp,
                    null,
                    null
                ) else mp

            // It's not to be the IL retail way, but it make the message more logical
            if (obj.currentMp + mp >= obj.maxMp)
                mp = obj.maxMp - obj.currentMp

            obj.currentMp = mp + obj.currentMp
            val sump = StatusUpdate(obj)
            sump.addAttribute(StatusUpdate.CUR_MP, obj.currentMp.toInt())
            obj.sendPacket(sump)

            if (activeChar is Player && activeChar !== obj)
                obj.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S2_MP_RESTORED_BY_S1).addCharName(
                        activeChar
                    ).addNumber(mp.toInt())
                )
            else
                obj.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_MP_RESTORED).addNumber(mp.toInt()))
        }

        if (skill.hasSelfEffects()) {
            val effect = activeChar.getFirstEffect(skill.id)
            if (effect != null && effect.isSelfEffect)
                effect.exit()

            skill.getEffectsSelf(activeChar)
        }

        if (!skill.isPotion)
            activeChar.setChargedShot(
                if (activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT)) ShotType.BLESSED_SPIRITSHOT else ShotType.SPIRITSHOT,
                skill.isStaticReuse
            )
    }

    companion object {
        private val SKILL_IDS = arrayOf(L2SkillType.MANAHEAL, L2SkillType.MANARECHARGE)
    }
}