package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.skills.Env

class ConditionPlayerPledgeClass
    (private val _pledgeClass: Int) : Condition() {

    override fun testImpl(env: Env): Boolean {
        if (env.player == null)
            return false

        if (env.player!!.clan == null)
            return false

        return if (_pledgeClass == -1) env.player!!.isClanLeader else env.player!!.pledgeClass >= _pledgeClass
    }
}