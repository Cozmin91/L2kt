package com.l2kt.gameserver.handler.chathandlers

import com.l2kt.gameserver.handler.IChatHandler
import com.l2kt.gameserver.model.BlockList
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.CreatureSay

class ChatTell : IChatHandler {

    override fun handleChat(type: Int, activeChar: Player, target: String, text: String) {
        if (target.isEmpty())
            return

        val receiver = World.getPlayer(target)
        if (receiver == null || receiver.client!!.isDetached) {
            activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME)
            return
        }

        if (activeChar == receiver) {
            activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
            return
        }

        if (receiver.isInJail || receiver.isChatBanned) {
            activeChar.sendPacket(SystemMessageId.TARGET_IS_CHAT_BANNED)
            return
        }

        if (!activeChar.isGM && (receiver.isInRefusalMode || BlockList.isBlocked(receiver, activeChar))) {
            activeChar.sendPacket(SystemMessageId.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE)
            return
        }

        receiver.sendPacket(CreatureSay(activeChar.objectId, type, activeChar.name, text))
        activeChar.sendPacket(CreatureSay(activeChar.objectId, type, "->" + receiver.name, text))
    }

    override val chatTypeList: IntArray get() = COMMAND_IDS

    companion object {
        private val COMMAND_IDS = intArrayOf(2)
    }
}