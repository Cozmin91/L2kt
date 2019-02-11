package com.l2kt.gameserver.skills.l2skills

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.skills.effects.EffectSeed
import com.l2kt.gameserver.templates.StatsSet
import com.l2kt.gameserver.templates.skills.L2EffectType

class L2SkillSeed(set: StatsSet) : L2Skill(set) {

    override fun useSkill(caster: Creature, targets: Array<WorldObject>) {
        if (caster.isAlikeDead)
            return

        for (obj in targets) {
            if (obj !is Creature)
                continue

            if (obj.isAlikeDead && targetType != L2Skill.SkillTargetType.TARGET_CORPSE_MOB)
                continue

            val oldEffect = obj.getFirstEffect(id) as EffectSeed
            oldEffect.increasePower()

            val effects = obj.allEffects
            for (effect in effects)
                if (effect.effectType === L2EffectType.SEED)
                    effect.rescheduleEffect()
        }
    }
}