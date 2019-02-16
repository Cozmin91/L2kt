package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.data.xml.AdminData
import com.l2kt.gameserver.handler.AdminCommandHandler
import java.util.logging.Logger

class SendBypassBuildCmd : L2GameClientPacket() {

    private var _command: String = ""

    override fun readImpl() {
        _command = readS().trim{ it <= ' ' }
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        val command = "admin_" + _command.split(" ").dropLastWhile { it.isEmpty() }.toTypedArray()[0]

        val ach = AdminCommandHandler.getHandler(command)
        if (ach == null) {
            if (player.isGM)
                player.sendMessage("The command " + command.substring(6) + " doesn't exist.")

            L2GameClientPacket.LOGGER.warn("No handler registered for admin command '{}'.", command)
            return
        }

        if (!AdminData.hasAccess(command, player.accessLevel)) {
            player.sendMessage("You don't have the access right to use this command.")
            L2GameClientPacket.LOGGER.warn(
                "{} tried to use admin command '{}', but has no access to use it.",
                player.name,
                command
            )
            return
        }

        if (Config.GMAUDIT)
            GMAUDIT_LOG.info(player.name + " [" + player.objectId + "] used '" + _command + "' command on: " + if (player.target != null) player.target.name else "none")

        ach.useAdminCommand("admin_" + _command, player)
    }

    companion object {
        private val GMAUDIT_LOG = Logger.getLogger("gmaudit")
    }
}