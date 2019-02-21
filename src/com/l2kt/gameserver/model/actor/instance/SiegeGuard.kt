package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.ai.type.CreatureAI
import com.l2kt.gameserver.model.actor.ai.type.SiegeGuardAI
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.entity.Siege.SiegeSide
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.MoveToPawn

/**
 * This class represents all guards in the world.
 */
class SiegeGuard(objectId: Int, template: NpcTemplate) : Attackable(objectId, template) {

    override fun getAI(): CreatureAI {
        return _ai ?: synchronized(this) {
            if (_ai == null)
                _ai = SiegeGuardAI(this)

            return _ai
        }
    }

    /**
     * Return True if a siege is in progress and the Creature attacker isn't a Defender.
     * @param attacker The Creature that the L2SiegeGuardInstance try to attack
     */
    override fun isAutoAttackable(attacker: Creature): Boolean {
        // Attackable during siege by all except defenders
        return attacker?.actingPlayer != null && castle != null && castle.siege.isInProgress && !castle.siege.checkSides(
            attacker.actingPlayer!!.clan,
            SiegeSide.DEFENDER,
            SiegeSide.OWNER
        )
    }

    override fun hasRandomAnimation(): Boolean {
        return false
    }

    /**
     * Note that super() is not called because guards need extra check to see if a player should interact or ATTACK them when clicked.
     */
    override fun onAction(player: Player) {
        // Set the target of the player
        if (player.target !== this)
            player.target = this
        else {
            if (isAutoAttackable(player)) {
                if (!isAlikeDead && Math.abs(player.z - z) < 600)
                // this max heigth difference might need some tweaking
                    player.ai.setIntention(CtrlIntention.ATTACK, this)
            } else {
                // Notify the Player AI with INTERACT
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

    override fun addDamageHate(attacker: Creature?, damage: Int, aggro: Int) {
        if (attacker == null)
            return

        if (attacker !is SiegeGuard)
            super.addDamageHate(attacker, damage, aggro)
    }

    override fun isGuard(): Boolean {
        return true
    }

    override fun getDriftRange(): Int {
        return 20
    }
}