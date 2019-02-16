package com.l2kt.gameserver.handler.chathandlers

import com.l2kt.gameserver.data.manager.PetitionManager
import com.l2kt.gameserver.handler.IChatHandler
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId

class ChatPetition : IChatHandler {

    override fun handleChat(type: Int, activeChar: Player, target: String, text: String) {
        if (!PetitionManager.isPlayerInConsultation(activeChar)) {
            activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_IN_PETITION_CHAT)
            return
        }

        PetitionManager.sendActivePetitionMessage(activeChar, text)
    }

    override val chatTypeList: IntArray get() = COMMAND_IDS

    companion object {
        private val COMMAND_IDS = intArrayOf(6, 7)
    }
}