package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.skills.Env

class ConditionPlayerHasClanHall
    (private val _clanHall: List<Int>) : Condition() {

    override fun testImpl(env: Env): Boolean {
        if (env.player == null)
            return false

        val clan = env.player!!.clan ?: return _clanHall.size == 1 && _clanHall[0] == 0

        return if (_clanHall.size == 1 && _clanHall[0] == -1) clan.hasHideout() else _clanHall.contains(clan.hideoutId)
    }
}