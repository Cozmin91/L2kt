package com.l2kt.gameserver.model

import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.handler.SkillHandler
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.network.serverpackets.MagicSkillLaunched
import com.l2kt.gameserver.network.serverpackets.MagicSkillUse
import com.l2kt.gameserver.skills.effects.EffectChanceSkillTrigger
import com.l2kt.gameserver.templates.skills.L2SkillType
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import java.util.logging.Logger

/**
 * CT2.3: Added support for allowing effect as a chance skill trigger (DrHouse)
 * @author kombat
 */
class ChanceSkillList(val owner: Creature) : ConcurrentHashMap<IChanceSkillTrigger, ChanceCondition>() {

    fun onHit(target: Creature, ownerWasHit: Boolean, wasCrit: Boolean) {
        var event: Int
        if (ownerWasHit) {
            event = ChanceCondition.EVT_ATTACKED or ChanceCondition.EVT_ATTACKED_HIT
            if (wasCrit)
                event = event or ChanceCondition.EVT_ATTACKED_CRIT
        } else {
            event = ChanceCondition.EVT_HIT
            if (wasCrit)
                event = event or ChanceCondition.EVT_CRIT
        }

        onChanceSkillEvent(event, target)
    }

    fun onEvadedHit(attacker: Creature) {
        onChanceSkillEvent(ChanceCondition.EVT_EVADED_HIT, attacker)
    }

    fun onSkillHit(target: Creature, ownerWasHit: Boolean, wasMagic: Boolean, wasOffensive: Boolean) {
        var event: Int
        if (ownerWasHit) {
            event = ChanceCondition.EVT_HIT_BY_SKILL
            if (wasOffensive) {
                event = event or ChanceCondition.EVT_HIT_BY_OFFENSIVE_SKILL
                event = event or ChanceCondition.EVT_ATTACKED
            } else {
                event = event or ChanceCondition.EVT_HIT_BY_GOOD_MAGIC
            }
        } else {
            event = ChanceCondition.EVT_CAST
            event = event or if (wasMagic) ChanceCondition.EVT_MAGIC else ChanceCondition.EVT_PHYSICAL
            event = event or if (wasOffensive) ChanceCondition.EVT_MAGIC_OFFENSIVE else ChanceCondition.EVT_MAGIC_GOOD
        }

        onChanceSkillEvent(event, target)
    }

    fun onStart() {
        onChanceSkillEvent(ChanceCondition.EVT_ON_START, owner)
    }

    fun onActionTime() {
        onChanceSkillEvent(ChanceCondition.EVT_ON_ACTION_TIME, owner)
    }

    fun onExit() {
        onChanceSkillEvent(ChanceCondition.EVT_ON_EXIT, owner)
    }

    fun onChanceSkillEvent(event: Int, target: Creature) {
        if (owner.isDead())
            return

        for ((trigger, cond) in entries) {

            if (cond.trigger(event)) {
                if (trigger is L2Skill)
                    makeCast(trigger, target)
                else if (trigger is EffectChanceSkillTrigger)
                    makeCast(trigger, target)
            }
        }
    }

    private fun makeCast(skill: L2Skill?, target: Creature) {
        var skill = skill
        try {
            if (skill!!.getWeaponDependancy(owner) && skill.checkCondition(owner, target, false)) {
                if (skill.triggersChanceSkill())
                // skill will trigger another skill, but only if its not chance skill
                {
                    skill = SkillTable.getInfo(skill.triggeredChanceId, skill.triggeredChanceLevel)
                    if (skill == null || skill.skillType === L2SkillType.NOTDONE)
                        return
                }

                if (owner.isSkillDisabled(skill))
                    return

                if (skill.reuseDelay > 0)
                    owner.disableSkill(skill, skill.reuseDelay.toLong())

                val targets = skill.getTargetList(owner, false, target)

                if (targets.isEmpty())
                    return

                val firstTarget = targets[0] as Creature

                owner.broadcastPacket(
                    MagicSkillLaunched(
                        owner,
                        skill.id,
                        skill.level,
                        targets.toList()
                    )
                )
                owner.broadcastPacket(MagicSkillUse(owner, firstTarget, skill.id, skill.level, 0, 0))

                // Launch the magic skill and calculate its effects
                // TODO: once core will support all possible effects, use effects (not handler)
                val handler = SkillHandler.getHandler(skill.skillType)
                if (handler != null)
                    handler.useSkill(owner, skill, targets)
                else
                    skill.useSkill(owner, targets)
            }
        } catch (e: Exception) {
            _log.log(Level.WARNING, "", e)
        }

    }

    private fun makeCast(effect: EffectChanceSkillTrigger?, target: Creature) {
        try {
            if (effect == null || !effect.triggersChanceSkill())
                return

            val triggered = SkillTable.getInfo(effect.triggeredChanceId, effect.triggeredChanceLevel) ?: return
            val caster = if (triggered.targetType == L2Skill.SkillTargetType.TARGET_SELF) owner else effect.effector

            if (caster == null || triggered.skillType === L2SkillType.NOTDONE || caster.isSkillDisabled(triggered))
                return

            if (triggered.reuseDelay > 0)
                caster.disableSkill(triggered, triggered.reuseDelay.toLong())

            val targets = triggered.getTargetList(caster, false, target)

            if (targets.isEmpty())
                return

            val firstTarget = targets[0] as Creature

            val handler = SkillHandler.getHandler(triggered.skillType)

            owner.broadcastPacket(
                MagicSkillLaunched(
                    owner,
                    triggered.id,
                    triggered.level,
                    targets.toList()
                )
            )
            owner.broadcastPacket(MagicSkillUse(owner, firstTarget, triggered.id, triggered.level, 0, 0))

            // Launch the magic skill and calculate its effects
            // TODO: once core will support all possible effects, use effects (not handler)
            if (handler != null)
                handler.useSkill(caster, triggered, targets)
            else
                triggered.useSkill(caster, targets)
        } catch (e: Exception) {
            _log.log(Level.WARNING, "", e)
        }

    }

    companion object {
        protected val _log = Logger.getLogger(ChanceSkillList::class.java.name)
    }
}