package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage

/**
 * This class handles following admin commands: - help path = shows /data/html/admin/path file to char, should not be used by GM's directly
 */
class AdminHelpPage : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (command.startsWith("admin_help")) {
            try {
                val `val` = command.substring(11)
                showHelpPage(activeChar, `val`)
            } catch (e: StringIndexOutOfBoundsException) {
            }

        }

        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf("admin_help")

        // FIXME: implement method to send html to player in Player directly
        // PUBLIC & STATIC so other classes from package can include it directly
        fun showHelpPage(targetChar: Player, filename: String) {
            val html = NpcHtmlMessage(0)
            html.setFile("data/html/admin/$filename")
            targetChar.sendPacket(html)
        }
    }
}