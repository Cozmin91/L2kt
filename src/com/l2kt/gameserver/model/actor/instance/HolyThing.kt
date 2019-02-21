package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.MoveToPawn

class HolyThing(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {

    override val isAttackable: Boolean
        get() = false

    override fun onAction(player: Player) {
        // Set the target of the player
        if (player.target !== this)
            player.target = this
        else {
            // Calculate the distance between the Player and the L2Npc
            if (!canInteract(player)) {
                // Notify the Player AI with INTERACT
                player.ai.setIntention(CtrlIntention.INTERACT, this)
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

    override fun onForcedAttack(player: Player) {
        player.sendPacket(ActionFailed.STATIC_PACKET)
    }

    override fun reduceCurrentHp(damage: Double, attacker: Creature, skill: L2Skill) {}

    override fun reduceCurrentHp(damage: Double, attacker: Creature, awake: Boolean, isDOT: Boolean, skill: L2Skill) {}
}
