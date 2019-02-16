package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.instance.Player
import java.util.*

class AdminKick : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (command == "admin_character_disconnect" || command == "admin_kick")
            disconnectCharacter(activeChar)

        if (command.startsWith("admin_kick")) {
            val st = StringTokenizer(command)
            if (st.countTokens() > 1) {
                st.nextToken()
                val player = st.nextToken()
                val plyr = World.getPlayer(player)
                if (plyr != null) {
                    plyr.logout(false)
                    activeChar.sendMessage(plyr.name + " have been kicked from server.")
                }
            }
        }

        if (command.startsWith("admin_kick_non_gm")) {
            var counter = 0

            for (player in World.players) {
                if (player.isGM)
                    continue

                counter++
                player.logout(false)
            }
            activeChar.sendMessage("A total of $counter players have been kicked.")
        }
        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf("admin_character_disconnect", "admin_kick", "admin_kick_non_gm")

        private fun disconnectCharacter(activeChar: Player) {
            val target = activeChar.target
            var player: Player? = null

            if (target is Player)
                player = target
            else
                return

            if (player == activeChar)
                activeChar.sendMessage("You cannot disconnect your own character.")
            else {
                activeChar.sendMessage(player.name + " have been kicked from server.")
                player.logout(false)
            }
        }
    }
}