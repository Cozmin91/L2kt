package com.l2kt.gameserver.handler.chathandlers

import com.l2kt.gameserver.handler.IChatHandler
import com.l2kt.gameserver.model.BlockList
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.FloodProtectors
import com.l2kt.gameserver.network.FloodProtectors.Action
import com.l2kt.gameserver.network.serverpackets.CreatureSay

class ChatAll : IChatHandler {

    override fun handleChat(type: Int, activeChar: Player, params: String, text: String) {
        if (!FloodProtectors.performAction(activeChar.client!!, Action.GLOBAL_CHAT))
            return

        val cs = CreatureSay(activeChar.objectId, type, activeChar.name, text)
        for (player in activeChar.getKnownTypeInRadius(Player::class.java, 1250)) {
            if (!BlockList.isBlocked(player, activeChar))
                player.sendPacket(cs)
        }
        activeChar.sendPacket(cs)
    }

    override val chatTypeList: IntArray get() = COMMAND_IDS

    companion object {
        private val COMMAND_IDS = intArrayOf(0)
    }
}