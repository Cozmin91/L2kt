package com.l2kt.gameserver.skills.l2skills

import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.EffectPoint
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.templates.StatsSet

class L2SkillSignet(set: StatsSet) : L2Skill(set) {
    private val _effectNpcId: Int = set.getInteger("effectNpcId", -1)
    var signetEffectId: Int = 0

    init {
        signetEffectId = set.getInteger("effectId", -1)
    }

    override fun useSkill(caster: Creature, targets: Array<WorldObject>) {
        if (caster.isAlikeDead)
            return

        val template = NpcData.getTemplate(_effectNpcId)
        val effectPoint = EffectPoint(IdFactory.getInstance().nextId, template, caster)
        effectPoint.currentHp = effectPoint.maxHp.toDouble()
        effectPoint.currentMp = effectPoint.maxMp.toDouble()

        var worldPosition: Location? = null
        if (caster is Player && targetType == L2Skill.SkillTargetType.TARGET_GROUND)
            worldPosition = caster.currentSkillWorldPosition

        getEffects(caster, effectPoint)

        effectPoint.setIsInvul(true)
        effectPoint.spawnMe(worldPosition ?: caster.position)
    }
}