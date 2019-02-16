package com.l2kt.gameserver.handler.usercommandhandlers

import com.l2kt.gameserver.handler.IUserCommandHandler
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.ExMultiPartyCommandChannelInfo

class ChannelListUpdate : IUserCommandHandler {

    override fun useUserCommand(id: Int, player: Player): Boolean {
        val party = player.party ?: return false

        val channel = party.commandChannel ?: return false

        player.sendPacket(ExMultiPartyCommandChannelInfo(channel))
        return true
    }

    override val userCommandList: IntArray get() = COMMAND_IDS

    companion object {
        private val COMMAND_IDS = intArrayOf(97)
    }
}