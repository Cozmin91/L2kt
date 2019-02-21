package com.l2kt.gameserver.model.actor.instance

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.ai.CtrlEvent

import com.l2kt.gameserver.model.actor.template.NpcTemplate

class PenaltyMonster(objectId: Int, template: NpcTemplate) : Monster(objectId, template) {
    private var _ptk: Player? = null

    override fun getMostHated(): Creature? {
        return if (_ptk != null) _ptk else super.getMostHated() // always attack only one person

    }

    fun setPlayerToKill(ptk: Player) {
        if (Rnd[100] <= 80)
            broadcastNpcSay("Your bait was too delicious! Now, I will kill you!")

        _ptk = ptk

        ai.notifyEvent(CtrlEvent.EVT_AGGRESSION, _ptk, Rnd[1, 100])
    }

    override fun doDie(killer: Creature): Boolean {
        if (!super.doDie(killer))
            return false

        if (Rnd[100] <= 75)
            broadcastNpcSay("I will tell fish not to take your bait!")

        return true
    }
}