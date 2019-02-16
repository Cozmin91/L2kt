package com.l2kt.gameserver.handler.usercommandhandlers

import com.l2kt.gameserver.handler.IUserCommandHandler
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class PartyInfo : IUserCommandHandler {

    override fun useUserCommand(id: Int, player: Player): Boolean {
        val party = player.party ?: return false

        player.sendPacket(SystemMessageId.PARTY_INFORMATION)
        player.sendPacket(party.lootRule.messageId)
        player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PARTY_LEADER_S1).addString(party.leader.name))
        player.sendMessage("Members: " + party.membersCount + "/9")
        player.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER)
        return true
    }

    override val userCommandList: IntArray get() = COMMAND_IDS

    companion object {
        private val COMMAND_IDS = intArrayOf(81)
    }
}