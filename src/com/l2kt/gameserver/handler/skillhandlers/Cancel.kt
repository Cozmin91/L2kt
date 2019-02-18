package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.Config
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.ShotType
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.skills.Formulas
import com.l2kt.gameserver.templates.skills.L2SkillType

class Cancel : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        // Delimit min/max % success.
        val minRate = if (skill.skillType === L2SkillType.CANCEL) 25 else 40
        val maxRate = if (skill.skillType === L2SkillType.CANCEL) 75 else 95

        // Get skill power (which is used as baseRate).
        val skillPower = skill.power

        for (obj in targets) {
            if (obj !is Creature)
                continue

            if (obj.isDead())
                continue

            var lastCanceledSkillId = 0
            var count = skill.maxNegatedEffects

            // Calculate the difference of level between skill level and victim, and retrieve the vuln/prof.
            val diffLevel = skill.magicLevel - obj.level
            val skillVuln = Formulas.calcSkillVulnerability(activeChar, obj, skill, skill.skillType)

            for (effect in obj.allEffects) {
                // Don't cancel null effects or toggles.
                if (effect.skill.isToggle)
                    continue

                // Mage && Warrior Bane drop only particular stacktypes.
                if (skill.skillType == L2SkillType.MAGE_BANE) {
                    if ("casting_time_down".equals(effect.stackType, ignoreCase = true))
                        break

                    if ("ma_up".equals(effect.stackType, ignoreCase = true))
                        break

                    continue
                }
                else if (skill.skillType == L2SkillType.WARRIOR_BANE) {
                    if ("attack_time_down".equals(effect.stackType, ignoreCase = true))
                        break

                    if ("speed_up".equals(effect.stackType, ignoreCase = true))
                        break

                    continue
                }

                // If that skill effect was already canceled, continue.
                if (effect.skill.id == lastCanceledSkillId)
                    continue

                // Calculate the success chance following previous variables.
                if (calcCancelSuccess(effect.period, diffLevel, skillPower, skillVuln, minRate, maxRate)) {
                    // Stores the last canceled skill for further use.
                    lastCanceledSkillId = effect.skill.id

                    // Exit the effect.
                    effect.exit()
                }

                // Remove 1 to the stack of buffs to remove.
                count--

                // If the stack goes to 0, then break the loop.
                if (count == 0)
                    break
            }

            // Possibility of a lethal strike
            Formulas.calcLethalHit(activeChar, obj, skill)
        }

        if (skill.hasSelfEffects()) {
            val effect = activeChar.getFirstEffect(skill.id)
            if (effect != null && effect.isSelfEffect)
                effect.exit()

            skill.getEffectsSelf(activeChar)
        }
        activeChar.setChargedShot(
            if (activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT)) ShotType.BLESSED_SPIRITSHOT else ShotType.SPIRITSHOT,
            skill.isStaticReuse
        )
    }

    companion object {
        private val SKILL_IDS = arrayOf(L2SkillType.CANCEL, L2SkillType.MAGE_BANE, L2SkillType.WARRIOR_BANE)

        private fun calcCancelSuccess(
            effectPeriod: Int,
            diffLevel: Int,
            baseRate: Double,
            vuln: Double,
            minRate: Int,
            maxRate: Int
        ): Boolean {
            var rate = ((2 * diffLevel).toDouble() + baseRate + (effectPeriod / 120).toDouble()) * vuln

            if (Config.DEVELOPER)
                ISkillHandler.Companion._log.info("calcCancelSuccess(): diffLevel:$diffLevel, baseRate:$baseRate, vuln:$vuln, total:$rate")

            if (rate < minRate)
                rate = minRate.toDouble()
            else if (rate > maxRate)
                rate = maxRate.toDouble()

            return Rnd[100] < rate
        }
    }
}