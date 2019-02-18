package com.l2kt.gameserver.model.actor.stat

import com.l2kt.gameserver.model.actor.Summon

open class SummonStat(activeChar: Summon) : PlayableStat(activeChar) {
    override val activeChar: Summon? get() = super.activeChar as Summon?
}