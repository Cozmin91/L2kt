package com.l2kt.gameserver.handler.chathandlers

import com.l2kt.gameserver.handler.IChatHandler
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.partymatching.PartyMatchRoomList
import com.l2kt.gameserver.network.serverpackets.CreatureSay

class ChatPartyMatchRoom : IChatHandler {

    override fun handleChat(type: Int, activeChar: Player, target: String, text: String) {
        if (!activeChar.isInPartyMatchRoom)
            return

        val room = PartyMatchRoomList.getPlayerRoom(activeChar) ?: return

        val cs = CreatureSay(activeChar.objectId, type, activeChar.name, text)
        for (member in room.partyMembers)
            member.sendPacket(cs)
    }

    override val chatTypeList: IntArray get() = COMMAND_IDS

    companion object {
        private val COMMAND_IDS = intArrayOf(14)
    }
}