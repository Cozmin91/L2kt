package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.handler.SkillHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.ShotType
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.Summon
import com.l2kt.gameserver.model.actor.instance.Door
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.instance.SiegeFlag
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.StatusUpdate
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.skills.Stats
import com.l2kt.gameserver.templates.skills.L2SkillType

class Heal : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        val handler = SkillHandler.getHandler(L2SkillType.BUFF)
        if (handler != null)
            handler!!.useSkill(activeChar, skill, targets)

        var power = skill.power + activeChar.calcStat(Stats.HEAL_PROFICIENCY, 0.0, null, null)

        val sps = activeChar.isChargedShot(ShotType.SPIRITSHOT)
        val bsps = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT)

        when (skill.skillType) {
            L2SkillType.HEAL_STATIC -> {
            }

            else -> {
                var staticShotBonus = 0.0
                var mAtkMul = 1 // mAtk multiplier

                if ((sps || bsps) && activeChar is Player && activeChar.actingPlayer!!.isMageClass || activeChar is Summon) {
                    staticShotBonus = skill.mpConsume.toDouble() // static bonus for spiritshots

                    if (bsps) {
                        mAtkMul = 4
                        staticShotBonus *= 2.4
                    } else
                        mAtkMul = 2
                } else if ((sps || bsps) && activeChar is Npc) {
                    staticShotBonus = 2.4 * skill.mpConsume // always blessed spiritshots
                    mAtkMul = 4
                } else {
                    // shot dynamic bonus
                    if (bsps)
                        mAtkMul *= 4
                    else
                        mAtkMul += 1
                }

                power += staticShotBonus + Math.sqrt((mAtkMul * activeChar.getMAtk(activeChar, null)).toDouble())

                if (!skill.isPotion)
                    activeChar.setChargedShot(
                        if (bsps) ShotType.BLESSED_SPIRITSHOT else ShotType.SPIRITSHOT,
                        skill.isStaticReuse
                    )
            }
        }

        var hp: Double
        for (obj in targets) {
            if (obj !is Creature)
                continue

            if (obj.isDead || obj.isInvul)
                continue

            if (obj is Door || obj is SiegeFlag)
                continue

            // Player holding a cursed weapon can't be healed and can't heal
            if (obj !== activeChar) {
                if (obj is Player && obj.isCursedWeaponEquipped)
                    continue
                else if (activeChar is Player && activeChar.isCursedWeaponEquipped)
                    continue
            }

            when (skill.skillType) {
                L2SkillType.HEAL_PERCENT -> hp = obj.maxHp * power / 100.0
                else -> {
                    hp = power
                    hp *= obj.calcStat(Stats.HEAL_EFFECTIVNESS, 100.0, null, null) / 100
                }
            }

            // If you have full HP and you get HP buff, u will receive 0HP restored message
            if (obj.currentHp + hp >= obj.maxHp)
                hp = obj.maxHp - obj.currentHp

            if (hp < 0)
                hp = 0.0

            obj.currentHp = hp + obj.currentHp
            val su = StatusUpdate(obj)
            su.addAttribute(StatusUpdate.CUR_HP, obj.currentHp.toInt())
            obj.sendPacket(su)

            if (obj is Player) {
                if (skill.id == 4051)
                    obj.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REJUVENATING_HP))
                else {
                    if (activeChar is Player && activeChar !== obj)
                        obj.sendPacket(
                            SystemMessage.getSystemMessage(SystemMessageId.S2_HP_RESTORED_BY_S1).addCharName(
                                activeChar
                            ).addNumber(hp.toInt())
                        )
                    else
                        obj.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED).addNumber(hp.toInt()))
                }
            }
        }
    }

    companion object {
        private val SKILL_IDS = arrayOf(L2SkillType.HEAL, L2SkillType.HEAL_STATIC)
    }
}