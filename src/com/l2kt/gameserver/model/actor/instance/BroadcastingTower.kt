package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ItemList
import java.util.*

/**
 * An instance type extending [Folk], used by Broadcasting Towers.<br></br>
 * <br></br>
 * Those NPCs allow [Player]s to spectate areas (sieges, olympiads).
 */
class BroadcastingTower(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {

    override fun onBypassFeedback(player: Player, command: String) {
        if (command.startsWith("observe")) {
            val st = StringTokenizer(command)
            st.nextToken()

            val cost = Integer.parseInt(st.nextToken())
            val x = Integer.parseInt(st.nextToken())
            val y = Integer.parseInt(st.nextToken())
            val z = Integer.parseInt(st.nextToken())

            if (command.startsWith("observeSiege") && CastleManager.getActiveSiege(x, y, z) == null) {
                player.sendPacket(SystemMessageId.ONLY_VIEW_SIEGE)
                return
            }

            if (player.reduceAdena("Broadcast", cost, this, true)) {
                player.enterObserverMode(x, y, z)
                player.sendPacket(ItemList(player, false))
            }
        } else
            super.onBypassFeedback(player, command)
    }

    override fun getHtmlPath(npcId: Int, `val`: Int): String {
        var filename = ""
        if (`val` == 0)
            filename = "" + npcId
        else
            filename = "$npcId-$`val`"

        return "data/html/observation/$filename.htm"
    }
}