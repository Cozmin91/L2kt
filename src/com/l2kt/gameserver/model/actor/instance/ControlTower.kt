package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.model.L2Spawn
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.entity.Siege.SiegeSide
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.MoveToPawn
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.util.*

class ControlTower(objectId: Int, template: NpcTemplate) : Npc(objectId, template) {
    private val _guards = ArrayList<L2Spawn>()

    var isActive = true
        private set

    override// Attackable during siege by attacker only
    val isAttackable: Boolean
        get() = castle != null && castle!!.siege.isInProgress

    val guards: List<L2Spawn>
        get() = _guards

    override fun isAutoAttackable(attacker: Creature): Boolean {
        // Attackable during siege by attacker only
        return attacker is Player && castle != null && castle!!.siege.isInProgress && castle!!.siege.checkSide(
            attacker.clan,
            SiegeSide.ATTACKER
        )
    }

    override fun onForcedAttack(player: Player) {
        onAction(player)
    }

    override fun onAction(player: Player) {
        // Set the target of the player
        if (player.target !== this)
            player.target = this
        else {
            if (isAutoAttackable(player) && Math.abs(player.z - z) < 100 && GeoEngine.canSeeTarget(player, this)) {
                // Notify the Player AI with INTERACT
                player.ai.setIntention(CtrlIntention.ATTACK, this)
            } else {
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

    override fun doDie(killer: Creature?): Boolean {
        if (castle != null) {
            val siege = castle!!.siege
            if (siege.isInProgress) {
                isActive = false

                for (spawn in _guards)
                    spawn.setRespawnState(false)

                _guards.clear()

                // If siege life controls reach 0, broadcast a message to defenders.
                if (siege.controlTowerCount == 0)
                    siege.announceToPlayers(
                        SystemMessage.getSystemMessage(SystemMessageId.TOWER_DESTROYED_NO_RESURRECTION),
                        false
                    )

                // Spawn a little version of it. This version is a simple NPC, cleaned on siege end.
                try {
                    val spawn = L2Spawn(NpcData.getTemplate(13003))
                    spawn.loc = position

                    val tower = spawn.doSpawn(false)
                    tower!!.castle = castle

                    siege.destroyedTowers.add(tower)
                } catch (e: Exception) {
                    WorldObject.LOGGER.error("Couldn't spawn the control tower.", e)
                }

            }
        }
        return super.doDie(killer)
    }

    fun registerGuard(guard: L2Spawn) {
        _guards.add(guard)
    }
}