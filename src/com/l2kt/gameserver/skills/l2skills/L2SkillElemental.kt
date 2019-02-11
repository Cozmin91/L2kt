package com.l2kt.gameserver.skills.l2skills

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.ShotType
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Formulas
import com.l2kt.gameserver.templates.StatsSet

class L2SkillElemental(set: StatsSet) : L2Skill(set) {
    private val _seeds: IntArray = IntArray(3)
    private val _seedAny: Boolean

    init {
        _seeds[0] = set.getInteger("seed1", 0)
        _seeds[1] = set.getInteger("seed2", 0)
        _seeds[2] = set.getInteger("seed3", 0)

        _seedAny = set.getInteger("seed_any", 0) == 1
    }

    override fun useSkill(activeChar: Creature, targets: Array<WorldObject>) {
        if (activeChar.isAlikeDead)
            return

        val sps = activeChar.isChargedShot(ShotType.SPIRITSHOT)
        val bsps = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT)

        for (obj in targets) {
            if (obj !is Creature)
                continue

            if (obj.isAlikeDead)
                continue

            var charged = true
            if (!_seedAny) {
                for (_seed in _seeds) {
                    if (_seed != 0) {
                        val e = obj.getFirstEffect(_seed)
                        if (e == null || !e.inUse) {
                            charged = false
                            break
                        }
                    }
                }
            } else {
                charged = false
                for (_seed in _seeds) {
                    if (_seed != 0) {
                        val e = obj.getFirstEffect(_seed)
                        if (e != null && e.inUse) {
                            charged = true
                            break
                        }
                    }
                }
            }

            if (!charged) {
                activeChar.sendMessage("Target is not charged by elements.")
                continue
            }

            val mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(obj, this))
            val shld = Formulas.calcShldUse(activeChar, obj, this)

            val damage = Formulas.calcMagicDam(activeChar, obj, this, shld, sps, bsps, mcrit).toInt()
            if (damage > 0) {
                obj.reduceCurrentHp(damage.toDouble(), activeChar, this)

                // Manage cast break of the target (calculating rate, sending message...)
                Formulas.calcCastBreak(obj, damage.toDouble())

                activeChar.sendDamageMessage(obj, damage, false, false, false)
            }

            // activate attacked effects, if any
            obj.stopSkillEffects(id)
            getEffects(activeChar, obj, Env(shld, sps, false, bsps))
        }

        activeChar.setChargedShot(if (bsps) ShotType.BLESSED_SPIRITSHOT else ShotType.SPIRITSHOT, isStaticReuse)
    }
}