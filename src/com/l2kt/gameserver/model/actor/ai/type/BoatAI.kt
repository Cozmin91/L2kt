package com.l2kt.gameserver.model.actor.ai.type

import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Boat
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.location.SpawnLocation
import com.l2kt.gameserver.network.serverpackets.VehicleDeparture
import com.l2kt.gameserver.network.serverpackets.VehicleInfo
import com.l2kt.gameserver.network.serverpackets.VehicleStarted

internal class BoatAI(boat: Boat) : CreatureAI(boat) {

    override fun moveTo(x: Int, y: Int, z: Int) {
        if (!actor.isMovementDisabled) {
            if (!_clientMoving)
                actor.broadcastPacket(VehicleStarted(actor as Boat, 1))

            _clientMoving = true
            actor.moveToLocation(x, y, z, 0)
            actor.broadcastPacket(VehicleDeparture(actor as Boat))
        }
    }

    override fun clientStopMoving(loc: SpawnLocation?) {
        if (actor.isMoving)
            actor.stopMove(loc)

        if (_clientMoving || loc != null) {
            _clientMoving = false
            actor.broadcastPacket(VehicleStarted(actor as Boat, 0))
            actor.broadcastPacket(VehicleInfo(actor as Boat))
        }
    }

    override fun describeStateToPlayer(player: Player) {
        if (_clientMoving)
            player.sendPacket(VehicleDeparture(actor as Boat))
    }

    override fun onIntentionAttack(target: Creature?) {}

    override fun onIntentionCast(skill: L2Skill, target: WorldObject?) {}

    override fun onIntentionFollow(target: Creature) {}

    override fun onIntentionPickUp(item: WorldObject) {}

    override fun onEvtAttacked(attacker: Creature?) {}

    override fun onEvtAggression(target: Creature?, aggro: Int) {}

    override fun onEvtStunned(attacker: Creature?) {}

    override fun onEvtSleeping(attacker: Creature?) {}

    override fun onEvtRooted(attacker: Creature?) {}

    override fun onEvtCancel() {}

    override fun onEvtDead() {}

    override fun onEvtFakeDeath() {}

    override fun onEvtFinishCasting() {}

    override fun clientActionFailed() {}

    override fun moveToPawn(pawn: WorldObject?, offset: Int) {}

    override fun clientStoppedMoving() {}
}