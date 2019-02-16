package com.l2kt.gameserver.handler.usercommandhandlers

import com.l2kt.gameserver.handler.IUserCommandHandler
import com.l2kt.gameserver.model.actor.instance.Player

class Mount : IUserCommandHandler {

    override fun useUserCommand(id: Int, activeChar: Player): Boolean {
        return activeChar.mountPlayer(activeChar.pet)
    }

    override val userCommandList: IntArray get() = COMMAND_IDS

    companion object {
        private val COMMAND_IDS = intArrayOf(61)
    }
}