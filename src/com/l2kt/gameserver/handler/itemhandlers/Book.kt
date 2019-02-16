package com.l2kt.gameserver.handler.itemhandlers

import com.l2kt.gameserver.handler.IItemHandler
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage

class Book : IItemHandler {
    override fun useItem(playable: Playable, item: ItemInstance, forceUse: Boolean) {
        if (playable !is Player)
            return

        val itemId = item.itemId

        val html = NpcHtmlMessage(0)
        html.setFile("data/html/help/$itemId.htm")
        html.setItemId(itemId)
        playable.sendPacket(html)

        playable.sendPacket(ActionFailed.STATIC_PACKET)
    }
}