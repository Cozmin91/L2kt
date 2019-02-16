package com.l2kt.gameserver.handler

import com.l2kt.gameserver.handler.chathandlers.*
import java.util.*

object ChatHandler {
    private val _entries = HashMap<Int, IChatHandler>()

    init {
        registerHandler(ChatAll())
        registerHandler(ChatAlliance())
        registerHandler(ChatClan())
        registerHandler(ChatHeroVoice())
        registerHandler(ChatParty())
        registerHandler(ChatPartyMatchRoom())
        registerHandler(ChatPartyRoomAll())
        registerHandler(ChatPartyRoomCommander())
        registerHandler(ChatPetition())
        registerHandler(ChatShout())
        registerHandler(ChatTell())
        registerHandler(ChatTrade())
    }

    private fun registerHandler(handler: IChatHandler) {
        for (id in handler.chatTypeList)
            _entries[id] = handler
    }

    fun getHandler(chatType: Int): IChatHandler? {
        return _entries[chatType]
    }

    fun size(): Int {
        return _entries.size
    }
}