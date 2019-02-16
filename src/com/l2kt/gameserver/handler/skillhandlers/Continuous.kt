package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.manager.DuelManager
import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.ShotType
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.ai.CtrlEvent
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.ClanHallManagerNpc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Formulas
import com.l2kt.gameserver.templates.skills.L2EffectType
import com.l2kt.gameserver.templates.skills.L2SkillType

class Continuous : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        var skill = skill
        val player = activeChar.actingPlayer

        if (skill.effectId != 0) {
            val sk = SkillTable.getInfo(skill.effectId, if (skill.effectLvl == 0) 1 else skill.effectLvl)
            if (sk != null)
                skill = sk
        }

        val ss = activeChar.isChargedShot(ShotType.SOULSHOT)
        val sps = activeChar.isChargedShot(ShotType.SPIRITSHOT)
        val bsps = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT)

        for (obj in targets) {
            if (obj !is Creature)
                continue

            var target = obj
            if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
                target = activeChar

            if (skill.skillType == L2SkillType.BUFF) {
                // Target under buff immunity.
                if (target.getFirstEffect(L2EffectType.BLOCK_BUFF) != null)
                    continue

                // Player holding a cursed weapon can't be buffed and can't buff
                if (activeChar !is ClanHallManagerNpc && target !== activeChar) {
                    if (target is Player) {
                        if (target.isCursedWeaponEquipped)
                            continue
                    } else if (player != null && player.isCursedWeaponEquipped)
                        continue
                }
            }
            else if (skill.skillType == L2SkillType.HOT || skill.skillType == L2SkillType.CPHOT || skill.skillType == L2SkillType.MPHOT) {
                if (activeChar.isInvul)
                    continue
            }

            // Target under debuff immunity.
            if (skill.isOffensive && target.getFirstEffect(L2EffectType.BLOCK_DEBUFF) != null)
                continue

            var acted = true
            var shld: Byte = 0

            if (skill.isOffensive || skill.isDebuff) {
                shld = Formulas.calcShldUse(activeChar, target, skill)
                acted = Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps)
            }

            if (acted) {
                if (skill.isToggle)
                    target.stopSkillEffects(skill.id)

                // if this is a debuff let the duel manager know about it so the debuff
                // can be removed after the duel (player & target must be in the same duel)
                if (target is Player && target.isInDuel && (skill.skillType === L2SkillType.DEBUFF || skill.skillType === L2SkillType.BUFF) && player != null && player.duelId == target.duelId) {
                    val dm = DuelManager
                    for (buff in skill.getEffects(activeChar, target, Env(shld, ss, sps, bsps)))
                        if (buff != null)
                            dm.onBuff(target, buff)
                } else
                    skill.getEffects(activeChar, target, Env(shld, ss, sps, bsps))

                if (skill.skillType === L2SkillType.AGGDEBUFF) {
                    if (target is Attackable)
                        target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, skill.power.toInt())
                    else if (target is Playable) {
                        if (target.getTarget() === activeChar)
                            target.getAI().setIntention(CtrlIntention.ATTACK, activeChar)
                        else
                            target.setTarget(activeChar)
                    }
                }
            } else
                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ATTACK_FAILED))

            // Possibility of a lethal strike
            Formulas.calcLethalHit(activeChar, target, skill)
        }

        if (skill.hasSelfEffects()) {
            val effect = activeChar.getFirstEffect(skill.id)
            if (effect != null && effect.isSelfEffect)
                effect.exit()

            skill.getEffectsSelf(activeChar)
        }

        if (!skill.isPotion)
            activeChar.setChargedShot(
                if (bsps) ShotType.BLESSED_SPIRITSHOT else ShotType.SPIRITSHOT,
                skill.isStaticReuse
            )
    }

    companion object {
        private val SKILL_IDS = arrayOf(
            L2SkillType.BUFF,
            L2SkillType.DEBUFF,
            L2SkillType.DOT,
            L2SkillType.MDOT,
            L2SkillType.POISON,
            L2SkillType.BLEED,
            L2SkillType.HOT,
            L2SkillType.CPHOT,
            L2SkillType.MPHOT,
            L2SkillType.FEAR,
            L2SkillType.CONT,
            L2SkillType.WEAKNESS,
            L2SkillType.REFLECT,
            L2SkillType.UNDEAD_DEFENSE,
            L2SkillType.AGGDEBUFF,
            L2SkillType.FUSION
        )
    }
}