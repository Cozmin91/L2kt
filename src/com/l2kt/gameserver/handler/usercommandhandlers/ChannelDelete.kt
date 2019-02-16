package com.l2kt.gameserver.handler.usercommandhandlers

import com.l2kt.gameserver.handler.IUserCommandHandler
import com.l2kt.gameserver.model.actor.instance.Player

class ChannelDelete : IUserCommandHandler {

    override fun useUserCommand(id: Int, player: Player): Boolean {
        val party = player.party
        if (party == null || !party.isLeader(player))
            return false

        val channel = party.commandChannel
        if (channel == null || !channel.isLeader(player))
            return false

        channel.disband()
        return true
    }

    override val userCommandList: IntArray get() = COMMAND_IDS

    companion object {
        private val COMMAND_IDS = intArrayOf(93)
    }
}