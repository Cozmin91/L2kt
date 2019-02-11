package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.skills.Env

class ConditionUsingItemType(private val _mask: Int) : Condition() {

    override fun testImpl(env: Env): Boolean {
        return if (env.character !is Player) false else _mask and env.player!!.inventory!!.wornMask != 0

    }
}