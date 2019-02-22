package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.ai.type.CreatureAI
import com.l2kt.gameserver.model.actor.ai.type.WalkerAI
import com.l2kt.gameserver.model.actor.template.NpcTemplate

/**
 * A Walker is a [Folk] which continuously walks, following a defined route. It got no other Intention than MOVE_TO, so it never stops walking/running - except for programmed timers - and can speak.<br></br>
 * <br></br>
 * It can't be killed, and the AI is never detached (works even when region is supposed to be down due to lack of players).
 */
class Walker(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {
    init {

        ai = WalkerAI(this)
    }

    override fun setAI(newAI: CreatureAI) {
        // AI can't be detached, npc must move with the same AI instance.
        if (_ai !is WalkerAI)
            _ai = newAI
    }

    override fun reduceCurrentHp(i: Double, attacker: Creature, awake: Boolean, isDOT: Boolean, skill: L2Skill) {}

    override fun doDie(killer: Creature?): Boolean {
        return false
    }

    override fun getAI(): WalkerAI {
        return _ai as WalkerAI
    }

    override fun detachAI() {
        // AI can't be detached.
    }
}