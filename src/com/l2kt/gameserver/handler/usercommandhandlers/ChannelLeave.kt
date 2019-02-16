package com.l2kt.gameserver.handler.usercommandhandlers

import com.l2kt.gameserver.handler.IUserCommandHandler
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class ChannelLeave : IUserCommandHandler {

    override fun useUserCommand(id: Int, player: Player): Boolean {
        val party = player.party
        if (party == null || !party.isLeader(player))
            return false

        val channel = party.commandChannel ?: return false

        channel.removeParty(party)

        party.broadcastMessage(SystemMessageId.LEFT_COMMAND_CHANNEL)
        channel.broadcastPacket(
            SystemMessage.getSystemMessage(SystemMessageId.S1_PARTY_LEFT_COMMAND_CHANNEL).addCharName(
                player
            )
        )
        return true
    }

    override val userCommandList: IntArray get() = COMMAND_IDS

    companion object {
        private val COMMAND_IDS = intArrayOf(96)
    }
}