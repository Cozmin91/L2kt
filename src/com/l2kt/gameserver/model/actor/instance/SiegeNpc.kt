package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import com.l2kt.gameserver.network.serverpackets.SiegeInfo

class SiegeNpc(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {

    override fun showChatWindow(player: Player) {
        if (!castle.siege.isInProgress)
            player.sendPacket(SiegeInfo(castle))
        else {
            val html = NpcHtmlMessage(objectId)
            html.setFile("data/html/siege/$npcId-busy.htm")
            html.replace("%castlename%", castle.name)
            html.replace("%objectId%", objectId)
            player.sendPacket(html)
            player.sendPacket(ActionFailed.STATIC_PACKET)
        }
    }
}