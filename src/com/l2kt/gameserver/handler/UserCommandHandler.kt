package com.l2kt.gameserver.handler

import com.l2kt.gameserver.handler.usercommandhandlers.*
import java.util.*

object UserCommandHandler {
    private val _entries = HashMap<Int, IUserCommandHandler>()

    init {
        registerHandler(ChannelDelete())
        registerHandler(ChannelLeave())
        registerHandler(ChannelListUpdate())
        registerHandler(ClanPenalty())
        registerHandler(ClanWarsList())
        registerHandler(DisMount())
        registerHandler(Escape())
        registerHandler(Loc())
        registerHandler(Mount())
        registerHandler(OlympiadStat())
        registerHandler(PartyInfo())
        registerHandler(SiegeStatus())
        registerHandler(Time())
    }

    private fun registerHandler(handler: IUserCommandHandler) {
        for (id in handler.userCommandList)
            _entries[id] = handler
    }

    fun getHandler(userCommand: Int): IUserCommandHandler? {
        return _entries[userCommand]
    }

    fun size(): Int {
        return _entries.size
    }
}