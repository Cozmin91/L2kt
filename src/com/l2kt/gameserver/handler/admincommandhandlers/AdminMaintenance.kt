package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.Config
import com.l2kt.commons.util.SysUtil
import com.l2kt.gameserver.LoginServerThread
import com.l2kt.gameserver.Shutdown
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.instance.Player

import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import com.l2kt.gameserver.taskmanager.GameTimeTaskManager
import com.l2kt.loginserver.network.gameserverpackets.ServerStatus

class AdminMaintenance : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (command == "admin_server")
            sendHtmlForm(activeChar)
        else if (command.startsWith("admin_server_shutdown")) {
            try {
                Shutdown.instance.startShutdown(activeChar, null!!, Integer.parseInt(command.substring(22)), false)
            } catch (e: StringIndexOutOfBoundsException) {
                sendHtmlForm(activeChar)
            }

        } else if (command.startsWith("admin_server_restart")) {
            try {
                Shutdown.instance.startShutdown(activeChar, null!!, Integer.parseInt(command.substring(21)), true)
            } catch (e: StringIndexOutOfBoundsException) {
                sendHtmlForm(activeChar)
            }

        } else if (command.startsWith("admin_server_abort")) {
            Shutdown.instance.abort(activeChar)
        } else if (command == "admin_server_gm_only") {
            LoginServerThread.serverStatus = ServerStatus.STATUS_GM_ONLY
            Config.SERVER_GMONLY = true

            activeChar.sendMessage("Server is now setted as GMonly.")
            sendHtmlForm(activeChar)
        } else if (command == "admin_server_all") {
            LoginServerThread.serverStatus = ServerStatus.STATUS_AUTO
            Config.SERVER_GMONLY = false

            activeChar.sendMessage("Server isn't setted as GMonly anymore.")
            sendHtmlForm(activeChar)
        } else if (command.startsWith("admin_server_max_player")) {
            try {
                val number = Integer.parseInt(command.substring(24))

                LoginServerThread.setMaxPlayer(number)
                activeChar.sendMessage("Server maximum player amount is setted to $number.")
                sendHtmlForm(activeChar)
            } catch (e: Exception) {
                activeChar.sendMessage("The parameter must be a valid number.")
            }

        }
        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf(
            "admin_server",

            "admin_server_shutdown", "admin_server_restart", "admin_server_abort",

            "admin_server_gm_only", "admin_server_all", "admin_server_max_player"
        )

        private fun sendHtmlForm(activeChar: Player) {
            val html = NpcHtmlMessage(0)
            html.setFile("data/html/admin/maintenance.htm")
            html.replace("%count%", World.players.size)
            html.replace("%used%", SysUtil.usedMemory)
            html.replace("%server_name%", LoginServerThread.serverName!!)
            html.replace("%status%", LoginServerThread.statusString)
            html.replace("%max_players%", LoginServerThread.maxPlayers)
            html.replace("%time%", GameTimeTaskManager.gameTimeFormated)
            activeChar.sendPacket(html)
        }
    }
}