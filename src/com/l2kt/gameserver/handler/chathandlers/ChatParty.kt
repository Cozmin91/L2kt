package com.l2kt.gameserver.handler.chathandlers

import com.l2kt.gameserver.handler.IChatHandler
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.CreatureSay

class ChatParty : IChatHandler {

    override fun handleChat(type: Int, player: Player, target: String, text: String) {
        val party = player.party ?: return

        party.broadcastPacket(CreatureSay(player.objectId, type, player.name, text))
    }

    override val chatTypeList: IntArray get() = COMMAND_IDS

    companion object {
        private val COMMAND_IDS = intArrayOf(3)
    }
}