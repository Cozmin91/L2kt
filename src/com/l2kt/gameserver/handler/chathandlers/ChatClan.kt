package com.l2kt.gameserver.handler.chathandlers

import com.l2kt.gameserver.handler.IChatHandler
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.CreatureSay

class ChatClan : IChatHandler {

    override fun handleChat(type: Int, activeChar: Player, target: String, text: String) {
        activeChar.clan?.broadcastToOnlineMembers(CreatureSay(activeChar.objectId, type, activeChar.name, text))
    }

    override val chatTypeList: IntArray get() = COMMAND_IDS

    companion object {
        private val COMMAND_IDS = intArrayOf(4)
    }
}