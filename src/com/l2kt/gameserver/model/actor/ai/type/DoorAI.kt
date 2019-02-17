package com.l2kt.gameserver.model.actor.ai.type

import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Door
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.model.location.SpawnLocation

internal class DoorAI(door: Door) : CreatureAI(door) {

    override fun onIntentionIdle() {}

    override fun onIntentionActive() {}

    override fun onIntentionRest() {}

    override fun onIntentionAttack(target: Creature?) {}

    override fun onIntentionCast(skill: L2Skill, target: WorldObject?) {}

    override fun onIntentionMoveTo(loc: Location) {}

    override fun onIntentionFollow(target: Creature) {}

    override fun onIntentionPickUp(item: WorldObject) {}

    override fun onEvtAttacked(attacker: Creature?) {}

    override fun onEvtAggression(target: Creature?, aggro: Int) {}

    override fun onEvtStunned(attacker: Creature?) {}

    override fun onEvtSleeping(attacker: Creature?) {}

    override fun onEvtRooted(attacker: Creature?) {}

    override fun onEvtReadyToAct() {}

    override fun onEvtArrived() {}

    override fun onEvtArrivedBlocked(loc: SpawnLocation?) {}

    override fun onEvtCancel() {}

    override fun onEvtDead() {}
}