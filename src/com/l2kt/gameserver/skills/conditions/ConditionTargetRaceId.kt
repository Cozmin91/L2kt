package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.skills.Env

class ConditionTargetRaceId(private val _raceIds: List<Int>) : Condition() {

    override fun testImpl(env: Env): Boolean {
        return if (env.target !is Npc) false else _raceIds.contains(((env.target as Npc).template as NpcTemplate).race.ordinal)

    }
}