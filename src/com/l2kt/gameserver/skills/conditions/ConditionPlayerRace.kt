package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.skills.Env

class ConditionPlayerRace(private val _race: ClassRace) : Condition() {

    override fun testImpl(env: Env): Boolean {
        return if (env.player == null) false else env.player!!.race == _race

    }
}