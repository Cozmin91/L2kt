package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.taskmanager.GameTimeTaskManager

class ConditionGameTime(private val _night: Boolean) : Condition() {

    override fun testImpl(env: Env): Boolean {
        return GameTimeTaskManager.isNight == _night
    }
}