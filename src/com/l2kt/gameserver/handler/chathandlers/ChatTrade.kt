package com.l2kt.gameserver.handler.chathandlers

import com.l2kt.gameserver.data.xml.MapRegionData
import com.l2kt.gameserver.handler.IChatHandler
import com.l2kt.gameserver.model.BlockList
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.FloodProtectors
import com.l2kt.gameserver.network.FloodProtectors.Action
import com.l2kt.gameserver.network.serverpackets.CreatureSay

class ChatTrade : IChatHandler {

    override fun handleChat(type: Int, activeChar: Player, target: String, text: String) {
        if (!FloodProtectors.performAction(activeChar.client, Action.TRADE_CHAT))
            return

        val cs = CreatureSay(activeChar.objectId, type, activeChar.name, text)
        val region = MapRegionData.getMapRegion(activeChar.x, activeChar.y)

        for (player in World.players) {
            if (!BlockList.isBlocked(player, activeChar) && region == MapRegionData.getMapRegion(player.x, player.y))
                player.sendPacket(cs)
        }
    }

    override val chatTypeList: IntArray get() = COMMAND_IDS

    companion object {
        private val COMMAND_IDS = intArrayOf(8)
    }
}