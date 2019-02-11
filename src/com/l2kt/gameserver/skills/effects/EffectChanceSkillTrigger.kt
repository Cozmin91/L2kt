package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.ChanceCondition
import com.l2kt.gameserver.model.IChanceSkillTrigger
import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectType

class EffectChanceSkillTrigger(env: Env, template: EffectTemplate) : L2Effect(env, template), IChanceSkillTrigger {
    private val _triggeredId: Int = template.triggeredId
    private val _triggeredLevel: Int = template.triggeredLevel
    private val _chanceCondition: ChanceCondition = template.chanceCondition

    override fun getEffectType(): L2EffectType {
        return L2EffectType.CHANCE_SKILL_TRIGGER
    }

    override fun onStart(): Boolean {
        effected.addChanceTrigger(this)
        effected.onStartChanceEffect()
        return super.onStart()
    }

    override fun onActionTime(): Boolean {
        effected.onActionTimeChanceEffect()
        return false
    }

    override fun onExit() {
        // trigger only if effect in use and successfully ticked to the end
        if (inUse && count == 0)
            effected.onExitChanceEffect()
        effected.removeChanceEffect(this)
        super.onExit()
    }

    override fun getTriggeredChanceId(): Int {
        return _triggeredId
    }

    override fun getTriggeredChanceLevel(): Int {
        return _triggeredLevel
    }

    override fun triggersChanceSkill(): Boolean {
        return _triggeredId > 1
    }

    override fun getTriggeredChanceCondition(): ChanceCondition {
        return _chanceCondition
    }
}