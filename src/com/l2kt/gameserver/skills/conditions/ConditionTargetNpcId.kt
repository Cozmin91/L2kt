package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Door
import com.l2kt.gameserver.skills.Env

class ConditionTargetNpcId
    (private val _npcIds: List<Int>) : Condition() {

    override fun testImpl(env: Env): Boolean {
        if (env.target is Npc)
            return _npcIds.contains((env.target as Npc).npcId)

        return if (env.target is Door) _npcIds.contains((env.target as Door).doorId) else false
    }
}