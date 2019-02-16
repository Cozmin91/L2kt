package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.gameserver.data.xml.AdminData
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.clientpackets.Say2
import com.l2kt.gameserver.network.serverpackets.CreatureSay

/**
 * This class handles following admin commands:
 *
 *  * gmchat : sends text to all online GM's
 *  * gmchat_menu : same as gmchat, but displays the admin panel after chat
 *
 */
class AdminGmChat : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (command.startsWith("admin_gmchat")) {
            try {
                AdminData.broadcastToGMs(
                    CreatureSay(
                        0,
                        Say2.ALLIANCE,
                        activeChar.name,
                        command.substring(if (command.startsWith("admin_gmchat_menu")) 18 else 13)
                    )
                )
            } catch (e: StringIndexOutOfBoundsException) {
                // empty message.. ignore
            }

            if (command.startsWith("admin_gmchat_menu"))
                AdminHelpPage.showHelpPage(activeChar, "main_menu.htm")
        }

        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf("admin_gmchat", "admin_gmchat_menu")
    }
}