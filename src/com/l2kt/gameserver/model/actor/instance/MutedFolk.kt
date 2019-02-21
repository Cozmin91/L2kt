package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.MoveToPawn

/**
 * This class leads the behavior of muted NPCs.<br></br>
 * Their behaviors are the same than NPCs, they just can't talk to player.<br></br>
 * Some specials instances, such as CabaleBuffers or TownPets got their own muted onAction.
 */
class MutedFolk(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {

    override fun onAction(player: Player) {
        // Set the target of the player
        if (player.target !== this)
            player.target = this
        else {
            // Check if the player is attackable (without a forced attack).
            if (isAutoAttackable(player))
                player.ai.setIntention(CtrlIntention.ATTACK, this)
            else {
                // Calculate the distance between the Player and this instance.
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
    }

    override fun onActionShift(player: Player) {
        // Check if the Player is a GM ; send him NPC infos if true.
        if (player.isGM)
            sendNpcInfos(player)

        if (player.target !== this)
            player.target = this
        else {
            if (isAutoAttackable(player)) {
                if (player.isInsideRadius(this, player.physicalAttackRange, false, false) && GeoEngine.canSeeTarget(
                        player,
                        this
                    )
                )
                    player.ai.setIntention(CtrlIntention.ATTACK, this)
                else
                    player.sendPacket(ActionFailed.STATIC_PACKET)
            } else {
                // Calculate the distance between the Player and the L2Npc
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
    }
}