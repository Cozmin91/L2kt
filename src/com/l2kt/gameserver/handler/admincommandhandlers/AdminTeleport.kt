package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.gameserver.data.xml.MapRegionData
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import java.util.*

/**
 * This class handles teleport admin commands
 */
class AdminTeleport : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        // runmod
        if (command == "admin_runmod" || command == "admin_instant_move")
            activeChar.teleMode = 1
        if (command == "admin_runmod tele")
            activeChar.teleMode = 2
        if (command == "admin_runmod norm")
            activeChar.teleMode = 0

        // teleport via panels
        if (command == "admin_tele")
            AdminHelpPage.showHelpPage(activeChar, "teleports.htm")
        if (command == "admin_tele_areas")
            AdminHelpPage.showHelpPage(activeChar, "tele/other.htm")

        // recalls / goto types
        if (command.startsWith("admin_goto") || command.startsWith("admin_teleportto")) {
            val st = StringTokenizer(command)
            if (st.countTokens() > 1) {
                st.nextToken()
                val plyr = st.nextToken()
                val player = World.getPlayer(plyr)
                if (player == null) {
                    activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
                    return false
                }

                teleportToCharacter(activeChar, player)
            }
        } else if (command.startsWith("admin_recall ")) {
            try {
                val targetName = command.substring(13)
                val player = World.getPlayer(targetName)
                if (player == null) {
                    activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
                    return false
                }

                teleportCharacter(player, activeChar.x, activeChar.y, activeChar.z)
            } catch (e: StringIndexOutOfBoundsException) {
            }

        } else if (command.startsWith("admin_recall_party")) {
            try {
                val targetName = command.substring(19)
                val player = World.getPlayer(targetName)
                if (player == null) {
                    activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
                    return false
                }

                val party = player.party
                if (party != null) {
                    for (member in party.members)
                        teleportCharacter(member, activeChar.x, activeChar.y, activeChar.z)

                    activeChar.sendMessage("You recall " + player.name + "'s party.")
                } else {
                    activeChar.sendMessage("You recall " + player.name + ", but he isn't in a party.")
                    teleportCharacter(player, activeChar.x, activeChar.y, activeChar.z)
                }
            } catch (e: StringIndexOutOfBoundsException) {
            }

        } else if (command.startsWith("admin_recall_clan")) {
            try {
                val targetName = command.substring(18)
                val player = World.getPlayer(targetName)
                if (player == null) {
                    activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
                    return false
                }

                val clan = player.clan
                if (clan != null) {
                    for (member in clan.onlineMembers)
                        teleportCharacter(member, activeChar.x, activeChar.y, activeChar.z)

                    activeChar.sendMessage("You recall " + player.name + "'s clan.")
                } else {
                    activeChar.sendMessage("You recall " + player.name + ", but he isn't a clan member.")
                    teleportCharacter(player, activeChar.x, activeChar.y, activeChar.z)
                }
            } catch (e: StringIndexOutOfBoundsException) {
            }

        } else if (command.startsWith("admin_move_to")) {
            try {
                val `val` = command.substring(14)
                teleportTo(activeChar, `val`)
            } catch (e: Exception) {
                // Case of empty or missing coordinates
                AdminHelpPage.showHelpPage(activeChar, "teleports.htm")
            }

        } else if (command.startsWith("admin_sendhome")) {
            val st = StringTokenizer(command)
            if (st.countTokens() > 1) {
                st.nextToken()
                val plyr = st.nextToken()
                val player = World.getPlayer(plyr)
                if (player == null) {
                    activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
                    return false
                }

                sendHome(player)
            } else {
                val target = activeChar.target
                var player: Player? = null

                // if target isn't a player, select yourself as target
                if (target is Player)
                    player = target
                else
                    player = activeChar

                sendHome(player)
            }
        }
        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf(
            "admin_runmod",
            "admin_instant_move",
            "admin_tele",
            "admin_tele_areas",
            "admin_goto",
            "admin_teleportto", // deprecated
            "admin_recall",
            "admin_recall_party",
            "admin_recall_clan",
            "admin_move_to",
            "admin_sendhome"
        )

        private fun sendHome(player: Player) {
            player.teleToLocation(MapRegionData.TeleportType.TOWN)
            player.setIsIn7sDungeon(false)
            player.sendMessage("A GM sent you at nearest town.")
        }

        private fun teleportTo(activeChar: Player, Cords: String) {
            try {
                val st = StringTokenizer(Cords)
                val x1 = st.nextToken()
                val x = Integer.parseInt(x1)
                val y1 = st.nextToken()
                val y = Integer.parseInt(y1)
                val z1 = st.nextToken()
                val z = Integer.parseInt(z1)

                activeChar.ai.setIntention(CtrlIntention.IDLE)
                activeChar.teleToLocation(x, y, z, 0)

                activeChar.sendMessage("You have been teleported to $Cords.")
            } catch (nsee: NoSuchElementException) {
                activeChar.sendMessage("Coordinates you entered as parameter [$Cords] are wrong.")
            }

        }

        private fun teleportCharacter(player: Player, x: Int, y: Int, z: Int) {
            player.ai.setIntention(CtrlIntention.IDLE)
            player.teleToLocation(x, y, z, 0)
            player.sendMessage("A GM is teleporting you.")
        }

        private fun teleportToCharacter(activeChar: Player, target: Player) {
            if (target.objectId == activeChar.objectId)
                activeChar.sendPacket(SystemMessageId.CANNOT_USE_ON_YOURSELF)
            else {
                val x = target.x
                val y = target.y
                val z = target.z

                activeChar.ai.setIntention(CtrlIntention.IDLE)
                activeChar.teleToLocation(x, y, z, 0)
                activeChar.sendMessage("You have teleported to " + target.name + ".")
            }
        }
    }
}