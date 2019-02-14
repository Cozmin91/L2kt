package com.l2kt.gameserver.skills.l2skills

import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.ShotType
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.instance.Cubic
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.StatusUpdate
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.skills.Formulas
import com.l2kt.gameserver.templates.StatsSet
import kotlin.experimental.and

class L2SkillDrain(set: StatsSet) : L2Skill(set) {
    private val _absorbPart: Float = set.getFloat("absorbPart", 0f)
    private val _absorbAbs: Int = set.getInteger("absorbAbs", 0)

    override fun useSkill(activeChar: Creature, targets: Array<WorldObject>) {
        if (activeChar.isAlikeDead)
            return

        val sps = activeChar.isChargedShot(ShotType.SPIRITSHOT)
        val bsps = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT)
        val isPlayable = activeChar is Playable

        for (obj in targets) {
            if (obj !is Creature)
                continue

            if (obj.isAlikeDead && targetType != L2Skill.SkillTargetType.TARGET_CORPSE_MOB)
                continue

            if (activeChar !== obj && obj.isInvul)
                continue // No effect on invulnerable chars unless they cast it themselves.

            val mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(obj, this))
            val shld = Formulas.calcShldUse(activeChar, obj, this)
            val damage = Formulas.calcMagicDam(activeChar, obj, this, shld, sps, bsps, mcrit).toInt()

            if (damage > 0) {
                var _drain: Int
                val _cp = obj.currentCp.toInt()
                val _hp = obj.currentHp.toInt()
                _drain = if (isPlayable && _cp > 0) {
                    if (damage < _cp)
                        0
                    else
                        damage - _cp
                } else if (damage > _hp)
                    _hp
                else
                    damage

                val hpAdd = (_absorbAbs + _absorbPart * _drain).toDouble()
                if (hpAdd > 0) {
                    val hp: Double = if (activeChar.currentHp + hpAdd > activeChar.maxHp) activeChar.maxHp.toDouble() else activeChar.currentHp + hpAdd

                    activeChar.currentHp = hp

                    val suhp = StatusUpdate(activeChar)
                    suhp.addAttribute(StatusUpdate.CUR_HP, hp.toInt())
                    activeChar.sendPacket(suhp)
                }

                // That section is launched for drain skills made on ALIVE targets.
                if (!obj.isDead || targetType != L2Skill.SkillTargetType.TARGET_CORPSE_MOB) {
                    // Manage cast break of the target (calculating rate, sending message...)
                    Formulas.calcCastBreak(obj, damage.toDouble())

                    activeChar.sendDamageMessage(obj, damage, mcrit, false, false)

                    if (hasEffects() && targetType != L2Skill.SkillTargetType.TARGET_CORPSE_MOB) {
                        // ignoring vengance-like reflections
                        if (Formulas.calcSkillReflect(obj, this) and Formulas.SKILL_REFLECT_SUCCEED > 0) {
                            activeChar.stopSkillEffects(id)
                            getEffects(obj, activeChar)
                            activeChar.sendPacket(
                                SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(
                                    id
                                )
                            )
                        } else {
                            // activate attacked effects, if any
                            obj.stopSkillEffects(id)
                            if (Formulas.calcSkillSuccess(activeChar, obj, this, shld, bsps))
                                getEffects(activeChar, obj)
                            else
                                activeChar.sendPacket(
                                    SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(
                                        obj
                                    ).addSkillName(id)
                                )
                        }
                    }
                    obj.reduceCurrentHp(damage.toDouble(), activeChar, this)
                }
            }
        }

        if (hasSelfEffects()) {
            val effect = activeChar.getFirstEffect(id)
            if (effect != null && effect.isSelfEffect)
                effect.exit()

            getEffectsSelf(activeChar)
        }

        activeChar.setChargedShot(if (bsps) ShotType.BLESSED_SPIRITSHOT else ShotType.SPIRITSHOT, isStaticReuse)
    }

    fun useCubicSkill(activeCubic: Cubic, targets: Array<WorldObject>) {
        for (obj in targets) {
            if (obj !is Creature)
                continue

            if (obj.isAlikeDead && targetType != L2Skill.SkillTargetType.TARGET_CORPSE_MOB)
                continue

            val mcrit = Formulas.calcMCrit(activeCubic.getMCriticalHit(obj, this))
            val shld = Formulas.calcShldUse(activeCubic.owner, obj, this)
            val damage = Formulas.calcMagicDam(activeCubic, obj, this, mcrit, shld).toInt()

            // Check to see if we should damage the target
            if (damage > 0) {
                val owner = activeCubic.owner
                val hpAdd = (_absorbAbs + _absorbPart * damage).toDouble()
                if (hpAdd > 0) {
                    val hp: Double = if (owner.currentHp + hpAdd > owner.maxHp) owner.maxHp.toDouble() else owner.currentHp + hpAdd

                    owner.setCurrentHp(hp)

                    val suhp = StatusUpdate(owner)
                    suhp.addAttribute(StatusUpdate.CUR_HP, hp.toInt())
                    owner.sendPacket(suhp)
                }

                // That section is launched for drain skills made on ALIVE targets.
                if (!obj.isDead || targetType != L2Skill.SkillTargetType.TARGET_CORPSE_MOB) {
                    obj.reduceCurrentHp(damage.toDouble(), activeCubic.owner, this)

                    // Manage cast break of the target (calculating rate, sending message...)
                    Formulas.calcCastBreak(obj, damage.toDouble())

                    owner.sendDamageMessage(obj, damage, mcrit, false, false)
                }
            }
        }
    }
}