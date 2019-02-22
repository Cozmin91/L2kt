package com.l2kt.gameserver.model.actor.instance

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.MoveToPawn
import java.util.concurrent.ScheduledFuture

class TownPet(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {
    private var _aiTask: ScheduledFuture<*>? = null

    init {
        setRunning()

        _aiTask = ThreadPool.scheduleAtFixedRate(RandomWalkTask(), 1000, 10000)
    }

    override fun onAction(player: Player) {
        // Set the target of the player
        if (player.target !== this)
            player.target = this
        else {
            if (!canInteract(player))
                player.ai.setIntention(CtrlIntention.INTERACT, this)
            else {
                // Stop moving if we're already in interact range.
                if (player.isMoving || player.isInCombat)
                    player.ai.setIntention(CtrlIntention.IDLE)

                // Rotate the player to face the instance
                player.sendPacket(MoveToPawn(player, this, Npc.INTERACTION_DISTANCE))

                // Send ActionFailed to the player in order to avoid he stucks
                player.sendPacket(ActionFailed.STATIC_PACKET)
            }
        }
    }

    override fun deleteMe() {
        if (_aiTask != null) {
            _aiTask!!.cancel(true)
            _aiTask = null
        }
        super.deleteMe()
    }

    inner class RandomWalkTask : Runnable {
        override fun run() {
            if (spawn == null)
                return

            ai.setIntention(
                CtrlIntention.MOVE_TO,
                GeoEngine.canMoveToTargetLoc(x, y, z, spawn!!.locX + Rnd[-75, 75], spawn!!.locY + Rnd[-75, 75], z)
            )
        }
    }
}