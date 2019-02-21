package com.l2kt.gameserver.model.actor.instance

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlIntention

import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.MoveToPawn
import com.l2kt.gameserver.scripting.EventType
import com.l2kt.gameserver.scripting.Quest

/**
 * This class manages all Guards in the world.<br></br>
 * It inherits all methods from L2Attackable and adds some more such as:
 *
 *  * tracking PK
 *  * aggressive L2MonsterInstance.
 *
 */
class Guard(objectId: Int, template: NpcTemplate) : Attackable(objectId, template) {

    override fun isAutoAttackable(attacker: Creature): Boolean {
        return attacker is Monster
    }

    override fun onSpawn() {
        setIsNoRndWalk(true)
        super.onSpawn()
    }

    override fun getHtmlPath(npcId: Int, `val`: Int): String {
        var filename = ""
        if (`val` == 0)
            filename = "" + npcId
        else
            filename = "$npcId-$`val`"

        return "data/html/guard/$filename.htm"
    }

    override fun onAction(player: Player) {
        // Set the target of the player
        if (player.target !== this)
            player.target = this
        else {
            // Calculate the distance between the Player and the L2Npc
            if (!canInteract(player)) {
                // Set the Player Intention to INTERACT
                player.ai.setIntention(CtrlIntention.INTERACT, this)
            } else {
                // Stop moving if we're already in interact range.
                if (player.isMoving || player.isInCombat)
                    player.ai.setIntention(CtrlIntention.IDLE)

                // Rotate the player to face the instance
                player.sendPacket(MoveToPawn(player, this, Npc.INTERACTION_DISTANCE))

                // Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
                player.sendPacket(ActionFailed.STATIC_PACKET)

                // Some guards have no HTMs on retail. Bypass the chat window if such guard is met.
                when (npcId) {
                    30733 // Guards in start villages
                        , 31032, 31033, 31034, 31035, 31036, 31671 // Patrols
                        , 31672, 31673, 31674 -> return
                }

                if (hasRandomAnimation())
                    onRandomAnimation(Rnd[8])

                var scripts: List<Quest>? = template.getEventQuests(EventType.QUEST_START)
                if (scripts != null && !scripts.isEmpty())
                    player.lastQuestNpcObject = objectId

                scripts = template.getEventQuests(EventType.ON_FIRST_TALK)
                if (scripts != null && scripts.size == 1)
                    scripts[0].notifyFirstTalk(this, player)
                else
                    showChatWindow(player)
            }
        }
    }

    override fun isGuard(): Boolean {
        return true
    }

    override fun getDriftRange(): Int {
        return 20
    }
}