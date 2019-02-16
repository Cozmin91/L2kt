package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.data.manager.ZoneManager
import com.l2kt.gameserver.data.xml.MapRegionData
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import java.util.*

class AdminZone : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {

        val st = StringTokenizer(command, " ")
        val actualCommand = st.nextToken() // Get actual command

        if (actualCommand.equals("admin_zone_check", ignoreCase = true))
            showHtml(activeChar)
        else if (actualCommand.equals("admin_zone_visual", ignoreCase = true)) {
            try {
                val next = st.nextToken()
                if (next.equals("all", ignoreCase = true)) {
                    for (zone in ZoneManager.getZones(activeChar))
                        zone.visualizeZone(activeChar.z)

                    showHtml(activeChar)
                } else if (next.equals("clear", ignoreCase = true)) {
                    ZoneManager.clearDebugItems()
                    showHtml(activeChar)
                } else {
                    val zoneId = Integer.parseInt(next)
                    ZoneManager.getZoneById(zoneId)!!.visualizeZone(activeChar.z)
                }
            } catch (e: Exception) {
                activeChar.sendMessage("Invalid parameter for //zone_visual.")
            }

        }

        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf("admin_zone_check", "admin_zone_visual")

        private fun showHtml(player: Player) {
            val x = player.x
            val y = player.y
            val rx = (x - World.WORLD_X_MIN) / World.TILE_SIZE + World.TILE_X_MIN
            val ry = (y - World.WORLD_Y_MIN) / World.TILE_SIZE + World.TILE_Y_MIN

            val html = NpcHtmlMessage(0)
            html.setFile("data/html/admin/zone.htm")

            html.replace(
                "%MAPREGION%",
                "[x:" + MapRegionData.getMapRegionX(x) + " y:" + MapRegionData.getMapRegionY(y) + "]"
            )
            html.replace("%GEOREGION%", rx.toString() + "_" + ry)
            html.replace("%CLOSESTTOWN%", MapRegionData.getClosestTownName(x, y))
            html.replace("%CURRENTLOC%", x.toString() + ", " + y + ", " + player.z)

            val sb = StringBuilder(100)

            for (zone in ZoneId.VALUES) {
                if (player.isInsideZone(zone))
                    StringUtil.append(sb, zone, "<br1>")
            }
            html.replace("%ZONES%", sb.toString())

            // Reset the StringBuilder for another use.
            sb.setLength(0)

            for (zone in World.getRegion(x, y)!!.zones) {
                if (zone.isCharacterInZone(player))
                    StringUtil.append(sb, zone.id, " ")
            }
            html.replace("%ZLIST%", sb.toString())
            player.sendPacket(html)
        }
    }
}