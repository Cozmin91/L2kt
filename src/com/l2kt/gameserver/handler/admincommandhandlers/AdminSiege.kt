package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.instancemanager.AuctionManager
import com.l2kt.gameserver.instancemanager.ClanHallManager
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.entity.Castle
import com.l2kt.gameserver.model.entity.ClanHall
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import com.l2kt.gameserver.network.serverpackets.SiegeInfo
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.util.*

/**
 * This class handles all siege commands
 */
class AdminSiege : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        var command = command
        val st = StringTokenizer(command, " ")
        command = st.nextToken() // Get actual command

        // Get castle
        var castle: Castle? = null
        var clanhall: ClanHall? = null

        if (command.startsWith("admin_clanhall"))
            clanhall = ClanHallManager.getInstance().getClanHallById(Integer.parseInt(st.nextToken()))
        else if (st.hasMoreTokens())
            castle = CastleManager.getCastleByName(st.nextToken())

        if (clanhall == null && (castle == null || castle.castleId < 0)) {
            showCastleSelectPage(activeChar)
            return true
        }

        val target = activeChar.target
        var player: Player? = null
        if (target is Player)
            player = target

        if (castle != null) {
            if (command.equals("admin_add_attacker", ignoreCase = true)) {
                if (player == null)
                    activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT)
                else
                    castle.siege.registerAttacker(player)
            } else if (command.equals("admin_add_defender", ignoreCase = true)) {
                if (player == null)
                    activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT)
                else
                    castle.siege.registerDefender(player)
            } else if (command.equals("admin_clear_siege_list", ignoreCase = true)) {
                castle.siege.clearAllClans()
            } else if (command.equals("admin_endsiege", ignoreCase = true)) {
                castle.siege.endSiege()
            } else if (command.equals("admin_list_siege_clans", ignoreCase = true)) {
                activeChar.sendPacket(SiegeInfo(castle))
                return true
            } else if (command.equals("admin_move_defenders", ignoreCase = true)) {
                activeChar.sendPacket(SystemMessage.sendString("Not implemented yet."))
            } else if (command.equals("admin_setcastle", ignoreCase = true)) {
                if (player == null || player.clan == null)
                    activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT)
                else if (player.clan.hasCastle())
                    activeChar.sendMessage(player.name + "'s clan already owns a castle.")
                else
                    castle.setOwner(player.clan)
            } else if (command.equals("admin_removecastle", ignoreCase = true)) {
                if (castle.ownerId > 0)
                    castle.removeOwner()
                else
                    activeChar.sendMessage("This castle does not have an owner.")
            } else if (command.equals("admin_spawn_doors", ignoreCase = true)) {
                castle.spawnDoors(false)
            } else if (command.equals("admin_startsiege", ignoreCase = true)) {
                castle.siege.startSiege()
            } else if (command.equals("admin_reset_certificates", ignoreCase = true)) {
                castle.setLeftCertificates(300, true)
            }

            val html = NpcHtmlMessage(0)
            html.setFile("data/html/admin/castle.htm")
            html.replace("%castleName%", castle.name)
            html.replace("%circletId%", castle.circletId)
            html.replace("%artifactId%", castle.artifacts.toString())
            html.replace("%ticketsNumber%", castle.tickets.size)
            html.replace("%droppedTicketsNumber%", castle.droppedTickets.size)
            html.replace("%npcsNumber%", castle.relatedNpcIds.size)
            html.replace("%certificates%", castle.leftCertificates)

            val sb = StringBuilder()

            // Feed Control Tower infos.
            for (spawn in castle.controlTowers) {
                val teleLoc = spawn.toString().replace(",", "")
                StringUtil.append(sb, "<a action=\"bypass -h admin_move_to ", teleLoc, "\">", teleLoc, "</a><br1>")
            }

            html.replace("%ct%", sb.toString())

            // Cleanup the sb to reuse it.
            sb.setLength(0)

            // Feed Flame Tower infos.
            for (spawn in castle.flameTowers) {
                val teleLoc = spawn.toString().replace(",", "")
                StringUtil.append(sb, "<a action=\"bypass -h admin_move_to ", teleLoc, "\">", teleLoc, "</a><br1>")
            }

            html.replace("%ft%", sb.toString())

            activeChar.sendPacket(html)
        } else if (clanhall != null) {
            if (command.equals("admin_clanhallset", ignoreCase = true)) {
                if (player == null || player.clan == null)
                    activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT)
                else if (!ClanHallManager.getInstance().isFree(clanhall.id))
                    activeChar.sendMessage("This ClanHall isn't free!")
                else if (!player.clan.hasHideout()) {
                    ClanHallManager.getInstance().setOwner(clanhall.id, player.clan)
                    if (AuctionManager.getAuction(clanhall.id) != null)
                        AuctionManager.getAuction(clanhall.id)!!.deleteAuctionFromDB()
                } else
                    activeChar.sendMessage("You have already a ClanHall!")
            } else if (command.equals("admin_clanhalldel", ignoreCase = true)) {
                if (!ClanHallManager.getInstance().isFree(clanhall.id)) {
                    ClanHallManager.getInstance().setFree(clanhall.id)
                    AuctionManager.initNPC(clanhall.id)
                } else
                    activeChar.sendMessage("This ClanHall is already Free!")
            } else if (command.equals("admin_clanhallopendoors", ignoreCase = true)) {
                clanhall.openCloseDoors(true)
            } else if (command.equals("admin_clanhallclosedoors", ignoreCase = true)) {
                clanhall.openCloseDoors(false)
            } else if (command.equals("admin_clanhallteleportself", ignoreCase = true)) {
                val zone = clanhall.zone
                if (zone != null)
                    activeChar.teleToLocation(zone.randomLoc, 0)
            }

            val owner = ClanTable.getClan(clanhall.ownerId)

            val html = NpcHtmlMessage(0)
            html.setFile("data/html/admin/clanhall.htm")
            html.replace("%clanhallName%", clanhall.name)
            html.replace("%clanhallId%", clanhall.id)
            html.replace("%clanhallOwner%", owner?.name ?: "None")
            activeChar.sendPacket(html)
        }
        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf(
            "admin_siege",
            "admin_add_attacker",
            "admin_add_defender",
            "admin_list_siege_clans",
            "admin_clear_siege_list",
            "admin_move_defenders",
            "admin_spawn_doors",
            "admin_endsiege",
            "admin_startsiege",
            "admin_setcastle",
            "admin_removecastle",
            "admin_clanhall",
            "admin_clanhallset",
            "admin_clanhalldel",
            "admin_clanhallopendoors",
            "admin_clanhallclosedoors",
            "admin_clanhallteleportself",
            "admin_reset_certificates"
        )

        private fun showCastleSelectPage(activeChar: Player) {
            var i = 0

            val html = NpcHtmlMessage(0)
            html.setFile("data/html/admin/castles.htm")

            val sb = StringBuilder()
            for (castle in CastleManager.castles) {
                if (castle != null) {
                    StringUtil.append(
                        sb,
                        "<td fixwidth=90><a action=\"bypass -h admin_siege ",
                        castle.name,
                        "\">",
                        castle.name,
                        "</a></td>"
                    )
                    i++
                }

                if (i > 2) {
                    sb.append("</tr><tr>")
                    i = 0
                }
            }
            html.replace("%castles%", sb.toString())

            // Cleanup sb.
            sb.setLength(0)
            i = 0

            for (clanhall in ClanHallManager.getInstance().clanHalls.values) {
                if (clanhall != null) {
                    StringUtil.append(
                        sb,
                        "<td fixwidth=134><a action=\"bypass -h admin_clanhall ",
                        clanhall.id,
                        "\">",
                        clanhall.name,
                        "</a></td>"
                    )
                    i++
                }

                if (i > 1) {
                    sb.append("</tr><tr>")
                    i = 0
                }
            }
            html.replace("%clanhalls%", sb.toString())

            // Cleanup sb.
            sb.setLength(0)
            i = 0

            for (clanhall in ClanHallManager.getInstance().freeClanHalls.values) {
                if (clanhall != null) {
                    StringUtil.append(
                        sb,
                        "<td fixwidth=134><a action=\"bypass -h admin_clanhall ",
                        clanhall.id,
                        "\">",
                        clanhall.name,
                        "</a></td>"
                    )
                    i++
                }

                if (i > 1) {
                    sb.append("</tr><tr>")
                    i = 0
                }
            }
            html.replace("%freeclanhalls%", sb.toString())
            activeChar.sendPacket(html)
        }
    }
}