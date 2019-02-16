package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.gameserver.data.xml.DoorData
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.actor.instance.Door
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId

/**
 * This class handles following admin commands
 *
 *  * open = open a door using a doorId, or a targeted door if not found.
 *  * close = close a door using a doorId, or a targeted door if not found.
 *  * openall = open all doors registered on doors.xml.
 *  * closeall = close all doors registered on doors.xml.
 *
 */
class AdminDoorControl : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (command.startsWith("admin_open")) {
            if (command == "admin_openall") {
                for (door in DoorData.doors)
                    door.openMe()
            } else {
                try {
                    val door = DoorData.getDoor(Integer.parseInt(command.substring(11)))
                    if (door != null)
                        door.openMe()
                    else
                        activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
                } catch (e: Exception) {
                    val target = activeChar.target

                    if (target is Door)
                        target.openMe()
                    else
                        activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
                }

            }
        } else if (command.startsWith("admin_close")) {
            if (command == "admin_closeall") {
                for (door in DoorData.doors)
                    door.closeMe()
            } else {
                try {
                    val door = DoorData.getDoor(Integer.parseInt(command.substring(12)))
                    if (door != null)
                        door.closeMe()
                    else
                        activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
                } catch (e: Exception) {
                    val target = activeChar.target

                    if (target is Door)
                        target.closeMe()
                    else
                        activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
                }

            }
        }

        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf("admin_open", "admin_close", "admin_openall", "admin_closeall")
    }
}