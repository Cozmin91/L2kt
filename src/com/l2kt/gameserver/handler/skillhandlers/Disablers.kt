package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.ShotType
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Summon
import com.l2kt.gameserver.model.actor.ai.CtrlEvent
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.ai.type.AttackableAI
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.instance.SiegeSummon
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Formulas
import com.l2kt.gameserver.skills.Stats
import com.l2kt.gameserver.templates.skills.L2EffectType
import com.l2kt.gameserver.templates.skills.L2SkillType

class Disablers : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        val type = skill.skillType

        val ss = activeChar.isChargedShot(ShotType.SOULSHOT)
        val sps = activeChar.isChargedShot(ShotType.SPIRITSHOT)
        val bsps = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT)

        for (obj in targets) {
            if (obj !is Creature)
                continue

            var target = obj
            if (target.isDead || target.isInvul && !target.isParalyzed)
            // bypass if target is dead or invul (excluding invul from Petrification)
                continue

            if (skill.isOffensive && target.getFirstEffect(L2EffectType.BLOCK_DEBUFF) != null)
                continue

            val shld = Formulas.calcShldUse(activeChar, target, skill)

            when (type) {
                L2SkillType.BETRAY -> if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps))
                    skill.getEffects(activeChar, target, Env(shld, ss, sps, bsps))
                else
                    activeChar.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(
                            target
                        ).addSkillName(skill)
                    )

                L2SkillType.FAKE_DEATH ->
                    // stun/fakedeath is not mdef dependant, it depends on lvl difference, target CON and power of stun
                    skill.getEffects(activeChar, target, Env(shld, ss, sps, bsps))

                L2SkillType.ROOT, L2SkillType.STUN -> {
                    if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
                        target = activeChar

                    if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps))
                        skill.getEffects(activeChar, target, Env(shld, ss, sps, bsps))
                    else {
                        if (activeChar is Player)
                            activeChar.sendPacket(
                                SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(
                                    target
                                ).addSkillName(skill.id)
                            )
                    }
                }

                L2SkillType.SLEEP, L2SkillType.PARALYZE // use same as root for now
                -> {
                    if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
                        target = activeChar

                    if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps))
                        skill.getEffects(activeChar, target, Env(shld, ss, sps, bsps))
                    else {
                        if (activeChar is Player)
                            activeChar.sendPacket(
                                SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(
                                    target
                                ).addSkillName(skill.id)
                            )
                    }
                }

                L2SkillType.MUTE -> {
                    if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
                        target = activeChar

                    if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps)) {
                        // stop same type effect if available
                        val effects = target.allEffects
                        for (e in effects) {
                            if (e.skill.skillType === type)
                                e.exit()
                        }
                        skill.getEffects(activeChar, target, Env(shld, ss, sps, bsps))
                    } else {
                        if (activeChar is Player)
                            activeChar.sendPacket(
                                SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(
                                    target
                                ).addSkillName(skill.id)
                            )
                    }
                }

                L2SkillType.CONFUSION ->
                    // do nothing if not on mob
                    if (target is Attackable) {
                        if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps)) {
                            val effects = target.getAllEffects()
                            for (e in effects) {
                                if (e.skill.skillType === type)
                                    e.exit()
                            }
                            skill.getEffects(activeChar, target, Env(shld, ss, sps, bsps))
                        } else {
                            if (activeChar is Player)
                                activeChar.sendPacket(
                                    SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(
                                        target
                                    ).addSkillName(skill)
                                )
                        }
                    } else
                        activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT))

                L2SkillType.AGGDAMAGE -> {
                    if (target is Attackable)
                        target.getAI().notifyEvent(
                            CtrlEvent.EVT_AGGRESSION,
                            activeChar,
                            (150 * skill.power / (target.getLevel() + 7)).toInt()
                        )

                    skill.getEffects(activeChar, target, Env(shld, ss, sps, bsps))
                }

                L2SkillType.AGGREDUCE ->
                    // these skills needs to be rechecked
                    if (target is Attackable) {
                        skill.getEffects(activeChar, target, Env(shld, ss, sps, bsps))

                        val aggdiff = target.getHating(activeChar) - target.calcStat(
                            Stats.AGGRESSION,
                            target.getHating(activeChar).toDouble(),
                            target,
                            skill
                        )

                        if (skill.power > 0)
                            target.reduceHate(null, skill.power.toInt())
                        else if (aggdiff > 0)
                            target.reduceHate(null, aggdiff.toInt())
                    }

                L2SkillType.AGGREDUCE_CHAR ->
                    // these skills needs to be rechecked
                    if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps)) {
                        if (target is Attackable) {
                            val targ = target
                            targ.stopHating(activeChar)
                            if (targ.mostHated == null && targ.hasAI() && targ.ai is AttackableAI) {
                                (targ.ai as AttackableAI).setGlobalAggro(-25)
                                targ.aggroList.clear()
                                targ.ai.setIntention(CtrlIntention.ACTIVE)
                                targ.setWalking()
                            }
                        }
                        skill.getEffects(activeChar, target, Env(shld, ss, sps, bsps))
                    } else {
                        if (activeChar is Player)
                            activeChar.sendPacket(
                                SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(
                                    target
                                ).addSkillName(skill)
                            )

                        target.ai.notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar)
                    }

                L2SkillType.AGGREMOVE ->
                    // these skills needs to be rechecked
                    if (target is Attackable && !target.isRaidRelated()) {
                        if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps)) {
                            if (skill.targetType == L2Skill.SkillTargetType.TARGET_UNDEAD) {
                                if (target.isUndead())
                                    target.reduceHate(null, target.getHating(target.mostHated))
                            } else
                                target.reduceHate(null, target.getHating(target.mostHated))
                        } else {
                            if (activeChar is Player)
                                activeChar.sendPacket(
                                    SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(
                                        target
                                    ).addSkillName(skill)
                                )

                            target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar)
                        }
                    } else
                        target.ai.notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar)

                L2SkillType.ERASE ->
                    // doesn't affect siege summons
                    if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps) && target !is SiegeSummon) {
                        val summonOwner = (target as Summon).owner
                        val summonPet = summonOwner!!.pet
                        if (summonPet != null) {
                            summonPet.unSummon(summonOwner)
                            summonOwner.sendPacket(SystemMessageId.YOUR_SERVITOR_HAS_VANISHED)
                        }
                    } else {
                        if (activeChar is Player)
                            activeChar.sendPacket(
                                SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(
                                    target
                                ).addSkillName(skill)
                            )
                    }

                L2SkillType.CANCEL_DEBUFF -> {
                    val effects = target.allEffects

                    if (effects != null && !effects.isEmpty()){
                        var count = if (skill.maxNegatedEffects > 0) 0 else -2
                        for (e in effects) {
                            if (e == null || !e.skill.isDebuff || !e.skill.canBeDispeled())
                                continue

                            e.exit()

                            if (count > -1) {
                                count++
                                if (count >= skill.maxNegatedEffects)
                                    break
                            }
                        }
                    }
                }

                L2SkillType.NEGATE -> {
                    if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
                        target = activeChar

                    // Skills with negateId (skillId)
                    if (skill.negateId.size != 0) {
                        for (id in skill.negateId) {
                            if (id != 0)
                                target.stopSkillEffects(id)
                        }
                    } else {
                        val negateLvl = skill.negateLvl

                        for (e in target.allEffects) {
                            val effectSkill = e.skill
                            for (skillType in skill.negateStats) {
                                // If power is -1 the effect is always removed without lvl check
                                if (negateLvl == -1) {
                                    if (effectSkill.skillType === skillType || effectSkill.effectType != null && effectSkill.effectType === skillType)
                                        e.exit()
                                } else {
                                    if (effectSkill.effectType != null && effectSkill.effectAbnormalLvl >= 0) {
                                        if (effectSkill.effectType === skillType && effectSkill.effectAbnormalLvl <= negateLvl)
                                            e.exit()
                                    } else if (effectSkill.skillType === skillType && effectSkill.abnormalLvl <= negateLvl)
                                        e.exit()
                                }// Remove the effect according to its power.
                            }
                        }
                    }// All others negate type skills
                    skill.getEffects(activeChar, target, Env(shld, ss, sps, bsps))
                }
            }
        }

        if (skill.hasSelfEffects()) {
            val effect = activeChar.getFirstEffect(skill.id)
            if (effect != null && effect.isSelfEffect)
                effect.exit()

            skill.getEffectsSelf(activeChar)
        }
        activeChar.setChargedShot(if (bsps) ShotType.BLESSED_SPIRITSHOT else ShotType.SPIRITSHOT, skill.isStaticReuse)
    }

    companion object {
        private val SKILL_IDS = arrayOf(
            L2SkillType.STUN,
            L2SkillType.ROOT,
            L2SkillType.SLEEP,
            L2SkillType.CONFUSION,
            L2SkillType.AGGDAMAGE,
            L2SkillType.AGGREDUCE,
            L2SkillType.AGGREDUCE_CHAR,
            L2SkillType.AGGREMOVE,
            L2SkillType.MUTE,
            L2SkillType.FAKE_DEATH,
            L2SkillType.NEGATE,
            L2SkillType.CANCEL_DEBUFF,
            L2SkillType.PARALYZE,
            L2SkillType.ERASE,
            L2SkillType.BETRAY
        )
    }
}