package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.AdminForgePacket
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.util.*

/**
 * This class handles commands for gm to forge packets
 * @author Maktakien
 */
class AdminPForge : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (command == "admin_forge")
            showMainPage(activeChar)
        else if (command.startsWith("admin_forge2")) {
            try {
                val st = StringTokenizer(command)
                st.nextToken()
                val format = st.nextToken()
                showPage2(activeChar, format)
            } catch (ex: Exception) {
                activeChar.sendMessage("Usage: //forge2 format")
            }

        } else if (command.startsWith("admin_forge3")) {
            try {
                val st = StringTokenizer(command)
                st.nextToken()
                var format = st.nextToken()
                var broadcast = false

                if (format.toLowerCase() == "broadcast") {
                    format = st.nextToken()
                    broadcast = true
                }

                val sp = AdminForgePacket()
                for (i in 0 until format.length) {
                    var `val` = st.nextToken()
                    if (`val`.toLowerCase() == "\$objid") {
                        `val` = activeChar.objectId.toString()
                    } else if (`val`.toLowerCase() == "\$tobjid") {
                        `val` = activeChar.target?.objectId.toString()
                    } else if (`val`.toLowerCase() == "\$bobjid") {
                        if (activeChar.boat != null) {
                            `val` = activeChar.boat?.objectId.toString()
                        }
                    } else if (`val`.toLowerCase() == "\$clanid") {
                        `val` = activeChar.objectId.toString()
                    } else if (`val`.toLowerCase() == "\$allyid") {
                        `val` = activeChar.allyId.toString()
                    } else if (`val`.toLowerCase() == "\$tclanid") {
                        `val` = (activeChar.target as Player).objectId.toString()
                    } else if (`val`.toLowerCase() == "\$tallyid") {
                        `val` = (activeChar.target as Player).allyId.toString()
                    } else if (`val`.toLowerCase() == "\$x") {
                        `val` = activeChar.x.toString()
                    } else if (`val`.toLowerCase() == "\$y") {
                        `val` = activeChar.y.toString()
                    } else if (`val`.toLowerCase() == "\$z") {
                        `val` = activeChar.z.toString()
                    } else if (`val`.toLowerCase() == "\$heading") {
                        `val` = activeChar.heading.toString()
                    } else if (`val`.toLowerCase() == "\$tx") {
                        `val` = activeChar.target?.x.toString()
                    } else if (`val`.toLowerCase() == "\$ty") {
                        `val` = activeChar.target?.y.toString()
                    } else if (`val`.toLowerCase() == "\$tz") {
                        `val` = activeChar.target?.z.toString()
                    } else if (`val`.toLowerCase() == "\$theading") {
                        `val` = (activeChar.target as Player).heading.toString()
                    }

                    sp.addPart(format.toByteArray()[i], `val`)
                }

                if (broadcast)
                    activeChar.broadcastPacket(sp)
                else
                    activeChar.sendPacket(sp)

                showPage3(activeChar, format, command)
            } catch (ex: Exception) {
                activeChar.sendMessage("Usage: //forge or //forge2 format")
            }

        } else if (command.startsWith("admin_msg")) {
            try {
                // Used for testing SystemMessage IDs - Use //msg <ID>
                activeChar.sendPacket(SystemMessage.getSystemMessage(Integer.parseInt(command.substring(10).trim { it <= ' ' })))
            } catch (e: Exception) {
                activeChar.sendMessage("Command format: //msg <SYSTEM_MSG_ID>")
                return false
            }

        }
        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf("admin_forge", "admin_forge2", "admin_forge3", "admin_msg")

        private fun showMainPage(activeChar: Player) {
            AdminHelpPage.showHelpPage(activeChar, "pforge1.htm")
        }

        private fun showPage2(activeChar: Player, format: String) {
            val html = NpcHtmlMessage(0)
            html.setFile("data/html/admin/pforge2.htm")
            html.replace("%format%", format)

            val sb = StringBuilder()

            // First use of sb.
            for (i in 0 until format.length)
                StringUtil.append(sb, format[i], " : <edit var=\"v", i, "\" width=100><br1>")
            html.replace("%valueditors%", sb.toString())

            // Cleanup sb.
            sb.setLength(0)

            // Second use of sb.
            for (i in 0 until format.length)
                StringUtil.append(sb, " \\\$v", i)

            html.basicReplace("%send%", sb.toString())
            activeChar.sendPacket(html)
        }

        private fun showPage3(activeChar: Player, format: String, command: String) {
            val html = NpcHtmlMessage(0)
            html.setFile("data/html/admin/pforge3.htm")
            html.replace("%format%", format)
            html.replace("%command%", command)
            activeChar.sendPacket(html)
        }
    }
}