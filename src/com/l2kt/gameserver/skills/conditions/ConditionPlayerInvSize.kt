package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.skills.Env

class ConditionPlayerInvSize
    (private val _size: Int) : Condition() {

    override fun testImpl(env: Env): Boolean {
        return if (env.player != null) env.player!!.inventory!!.size <= env.player!!.inventoryLimit - _size else true

    }
}