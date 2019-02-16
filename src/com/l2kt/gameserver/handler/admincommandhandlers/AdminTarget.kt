package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId

/**
 * This class handles following admin commands: - target name = sets player with respective name as target
 */
class AdminTarget : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (command.startsWith("admin_target"))
            handleTarget(command, activeChar)
        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf("admin_target")

        private fun handleTarget(command: String, activeChar: Player) {
            try {
                val targetName = command.substring(13)
                val obj = World.getPlayer(targetName)

                if (obj != null)
                    obj.onAction(activeChar)
                else
                    activeChar.sendPacket(SystemMessageId.CONTACT_CURRENTLY_OFFLINE)
            } catch (e: IndexOutOfBoundsException) {
                activeChar.sendPacket(SystemMessageId.INCORRECT_CHARACTER_NAME_TRY_AGAIN)
            }

        }
    }
}