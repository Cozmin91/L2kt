package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.data.xml.DoorData
import com.l2kt.gameserver.data.xml.TeleportLocationData
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import java.util.*

/**
 * An instance type extending [Folk], used to open doors and teleport into specific locations. Used notably by Border Frontier captains, and Doorman (clan halls and castles).<br></br>
 * <br></br>
 * It has an active siege (false by default) and ownership (true by default) checks, which are overidden on children classes.<br></br>
 * <br></br>
 * It is the mother class of [ClanHallDoorman] and [CastleDoorman].
 */
open class Doorman(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {

    protected open val isUnderSiege: Boolean
        get() = false

    override fun onBypassFeedback(player: Player, command: String) {
        if (command.startsWith("open_doors")) {
            if (isOwnerClan(player)) {
                if (isUnderSiege) {
                    cannotManageDoors(player)
                    player.sendPacket(SystemMessageId.GATES_NOT_OPENED_CLOSED_DURING_SIEGE)
                } else
                    openDoors(player, command)
            }
        } else if (command.startsWith("close_doors")) {
            if (isOwnerClan(player)) {
                if (isUnderSiege) {
                    cannotManageDoors(player)
                    player.sendPacket(SystemMessageId.GATES_NOT_OPENED_CLOSED_DURING_SIEGE)
                } else
                    closeDoors(player, command)
            }
        } else if (command.startsWith("tele")) {
            if (isOwnerClan(player))
                doTeleport(player, command)
        } else
            super.onBypassFeedback(player, command)
    }

    override fun showChatWindow(player: Player) {
        val html = NpcHtmlMessage(objectId)
        html.setFile("data/html/doormen/" + template.npcId + if (!isOwnerClan(player)) "-no.htm" else ".htm")
        html.replace("%objectId%", objectId)
        player.sendPacket(html)

        player.sendPacket(ActionFailed.STATIC_PACKET)
    }

    protected open fun openDoors(player: Player, command: String) {
        val st = StringTokenizer(command.substring(10), ", ")
        st.nextToken()

        while (st.hasMoreTokens())
            DoorData.getDoor(Integer.parseInt(st.nextToken()))!!.openMe()
    }

    protected open fun closeDoors(player: Player, command: String) {
        val st = StringTokenizer(command.substring(11), ", ")
        st.nextToken()

        while (st.hasMoreTokens())
            DoorData.getDoor(Integer.parseInt(st.nextToken()))!!.closeMe()
    }

    protected fun cannotManageDoors(player: Player) {
        val html = NpcHtmlMessage(objectId)
        html.setFile("data/html/doormen/busy.htm")
        player.sendPacket(html)

        player.sendPacket(ActionFailed.STATIC_PACKET)
    }

    protected fun doTeleport(player: Player, command: String) {
        val list = TeleportLocationData.getTeleportLocation(Integer.parseInt(command.substring(5).trim { it <= ' ' }))
        if (list != null && !player.isAlikeDead)
            player.teleToLocation(list, 0)

        player.sendPacket(ActionFailed.STATIC_PACKET)
    }

    protected open fun isOwnerClan(player: Player): Boolean {
        return true
    }
}