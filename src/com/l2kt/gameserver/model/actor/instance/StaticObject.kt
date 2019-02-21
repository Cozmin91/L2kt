package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import com.l2kt.gameserver.network.serverpackets.ShowTownMap
import com.l2kt.gameserver.network.serverpackets.StaticObjectInfo

/**
 * A static object with low amount of interactions and no AI - such as throne, village town maps, etc.
 */
class StaticObject(objectId: Int) : WorldObject(objectId) {
    /**
     * @return the StaticObjectId.
     */
    /**
     * @param StaticObjectId The StaticObjectId to set.
     */
    var staticObjectId: Int = 0
    var type = -1 // 0 - map signs, 1 - throne , 2 - arena signs
    var isBusy: Boolean = false // True - if someone sitting on the throne
    var map: ShowTownMap? = null
        private set

    fun setMap(texture: String, x: Int, y: Int) {
        map = ShowTownMap("town_map.$texture", x, y)
    }

    override fun onAction(player: Player) {
        // Set the target of the player
        if (player.target !== this)
            player.target = this
        else {
            // Calculate the distance between the Player and the L2Npc
            if (!player.isInsideRadius(this, Npc.INTERACTION_DISTANCE, false, false)) {
                // Notify the Player AI with INTERACT
                player.ai.setIntention(CtrlIntention.INTERACT, this)
            } else {
                if (type == 2) {
                    val html = NpcHtmlMessage(objectId)
                    html.setFile("data/html/signboard.htm")
                    player.sendPacket(html)
                } else if (type == 0)
                    player.sendPacket(map)

                // Send ActionFailed to the player in order to avoid he stucks
                player.sendPacket(ActionFailed.STATIC_PACKET)
            }
        }
    }

    override fun onActionShift(player: Player) {
        if (player.isGM) {
            val html = NpcHtmlMessage(objectId)
            html.setFile("data/html/admin/staticinfo.htm")
            html.replace("%x%", x)
            html.replace("%y%", y)
            html.replace("%z%", z)
            html.replace("%objid%", objectId)
            html.replace("%staticid%", staticObjectId)
            html.replace("%class%", javaClass.simpleName)
            player.sendPacket(html)
            player.sendPacket(ActionFailed.STATIC_PACKET)
        }

        if (player.target !== this)
            player.target = this
        else
            player.sendPacket(ActionFailed.STATIC_PACKET)
    }

    override fun isAutoAttackable(attacker: Creature): Boolean {
        return false
    }

    override fun sendInfo(activeChar: Player) {
        activeChar.sendPacket(StaticObjectInfo(this))
    }
}