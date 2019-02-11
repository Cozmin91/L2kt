package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.skills.Env

class ConditionPlayerHasCastle
    (private val _castle: Int) : Condition() {

    override fun testImpl(env: Env): Boolean {
        if (env.player == null)
            return false

        val clan = env.player!!.clan ?: return _castle == 0

        return if (_castle == -1) clan.hasCastle() else clan.castleId == _castle
    }
}