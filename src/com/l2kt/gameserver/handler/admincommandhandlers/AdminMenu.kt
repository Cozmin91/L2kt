package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.instance.Player

class AdminMenu : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (command == "admin_char_manage")
            showMainPage(activeChar)
        else if (command.startsWith("admin_teleport_character_to_menu")) {
            val data = command.split(" ").dropLastWhile { it.isEmpty() }.toTypedArray()
            if (data.size == 5) {
                val playerName = data[1]
                val player = World.getPlayer(playerName)
                if (player != null)
                    teleportCharacter(
                        player,
                        Integer.parseInt(data[2]),
                        Integer.parseInt(data[3]),
                        Integer.parseInt(data[4]),
                        activeChar
                    )
            }
            showMainPage(activeChar)
        }

        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf("admin_char_manage", "admin_teleport_character_to_menu")

        private fun teleportCharacter(player: Player?, x: Int, y: Int, z: Int, activeChar: Player) {
            if (player != null) {
                player.sendMessage("A GM is teleporting you.")
                player.teleToLocation(x, y, z, 0)
            }
            showMainPage(activeChar)
        }

        private fun showMainPage(activeChar: Player) {
            AdminHelpPage.showHelpPage(activeChar, "charmanage.htm")
        }
    }
}