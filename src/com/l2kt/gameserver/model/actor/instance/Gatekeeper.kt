package com.l2kt.gameserver.model.actor.instance

import com.l2kt.Config
import com.l2kt.gameserver.data.cache.HtmCache
import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.data.xml.TeleportLocationData
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import java.util.*

/**
 * An instance type extending [Folk], used for teleporters.<br></br>
 * <br></br>
 * A teleporter allows [Player]s to teleport to a specific location, for a fee.
 */
class Gatekeeper(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {

    override fun getHtmlPath(npcId: Int, `val`: Int): String {
        var filename = ""
        if (`val` == 0)
            filename = "" + npcId
        else
            filename = "$npcId-$`val`"

        return "data/html/teleporter/$filename.htm"
    }

    override fun onBypassFeedback(player: Player, command: String) {
        // Generic PK check. Send back the HTM if found and cancel current action.
        if (!Config.KARMA_PLAYER_CAN_USE_GK && player.karma > 0 && showPkDenyChatWindow(player, "teleporter"))
            return

        if (command.startsWith("goto")) {
            val st = StringTokenizer(command, " ")
            st.nextToken()

            // No more tokens.
            if (!st.hasMoreTokens())
                return

            // No interaction possible with the NPC.
            if (!canInteract(player))
                return

            // Retrieve the list.
            val list = TeleportLocationData.getTeleportLocation(Integer.parseInt(st.nextToken())) ?: return

            // Siege is currently in progress in this location.
            if (CastleManager.getActiveSiege(list.x, list.y, list.z) != null) {
                player.sendPacket(SystemMessageId.CANNOT_PORT_VILLAGE_IN_SIEGE)
                return
            }

            // The list is for noble, but player isn't noble.
            if (list.isNoble && !player.isNoble) {
                val html = NpcHtmlMessage(objectId)
                html.setFile("data/html/teleporter/nobleteleporter-no.htm")
                html.replace("%objectId%", objectId)
                html.replace("%npcname%", name)
                player.sendPacket(html)

                player.sendPacket(ActionFailed.STATIC_PACKET)
                return
            }

            // Retrieve price list. Potentially cut it by 2 depending of current date.
            var price = list.price

            if (!list.isNoble) {
                val cal = Calendar.getInstance()
                if (cal.get(Calendar.HOUR_OF_DAY) in 20..23 && (cal.get(Calendar.DAY_OF_WEEK) == 1 || cal.get(
                        Calendar.DAY_OF_WEEK
                    ) == 7)
                )
                    price /= 2
            }

            // Delete related items, and if successful teleport the player to the location.
            if (player.destroyItemByItemId("Teleport ", if (list.isNoble) 6651 else 57, price, this, true))
                player.teleToLocation(list, 20)

            player.sendPacket(ActionFailed.STATIC_PACKET)
        } else if (command.startsWith("Chat")) {
            var `val` = 0
            try {
                `val` = Integer.parseInt(command.substring(5))
            } catch (ioobe: IndexOutOfBoundsException) {
            } catch (nfe: NumberFormatException) {
            }

            // Show half price HTM depending of current date. If not existing, use the regular "-1.htm".
            if (`val` == 1) {
                val cal = Calendar.getInstance()
                if (cal.get(Calendar.HOUR_OF_DAY) in 20..23 && (cal.get(Calendar.DAY_OF_WEEK) == 1 || cal.get(
                        Calendar.DAY_OF_WEEK
                    ) == 7)
                ) {
                    val html = NpcHtmlMessage(objectId)

                    var content: String? = HtmCache.getHtm("data/html/teleporter/half/$npcId.htm")
                    if (content == null)
                        content = HtmCache.getHtmForce("data/html/teleporter/$npcId-1.htm")

                    html.setHtml(content)
                    html.replace("%objectId%", objectId)
                    html.replace("%npcname%", name)
                    player.sendPacket(html)

                    player.sendPacket(ActionFailed.STATIC_PACKET)
                    return
                }
            }
            showChatWindow(player, `val`)
        } else
            super.onBypassFeedback(player, command)
    }

    override fun showChatWindow(player: Player, `val`: Int) {
        // Generic PK check. Send back the HTM if found and cancel current action.
        if (!Config.KARMA_PLAYER_CAN_USE_GK && player.karma > 0 && showPkDenyChatWindow(player, "teleporter"))
            return

        showChatWindow(player, getHtmlPath(npcId, `val`))
    }
}