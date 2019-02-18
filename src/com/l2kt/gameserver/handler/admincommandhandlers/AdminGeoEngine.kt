package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.geoengine.geodata.GeoStructure
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage

/**
 * @author -Nemesiss-, Hasha
 */
class AdminGeoEngine : IAdminCommandHandler {
    private val Y = "x "
    private val N = "   "

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (command.startsWith("admin_geo_bug")) {
            val geoX = GeoEngine.getGeoX(activeChar.x)
            val geoY = GeoEngine.getGeoY(activeChar.y)
            if (GeoEngine.hasGeoPos(geoX, geoY)) {
                try {
                    val comment = command.substring(14)
                    if (GeoEngine.addGeoBug(activeChar.position, activeChar.name + ": " + comment))
                        activeChar.sendMessage("GeoData bug saved.")
                } catch (e: Exception) {
                    activeChar.sendMessage("Usage: //admin_geo_bug comments")
                }

            } else
                activeChar.sendMessage("There is no geodata at this position.")
        } else if (command == "admin_geo_pos") {
            val geoX = GeoEngine.getGeoX(activeChar.x)
            val geoY = GeoEngine.getGeoY(activeChar.y)
            val rx = (activeChar.x - World.WORLD_X_MIN) / World.TILE_SIZE + World.TILE_X_MIN
            val ry = (activeChar.y - World.WORLD_Y_MIN) / World.TILE_SIZE + World.TILE_Y_MIN
            val block = GeoEngine.getBlock(geoX, geoY)
            activeChar.sendMessage("Region: " + rx + "_" + ry + "; Block: " + block.javaClass.simpleName)
            if (block.hasGeoPos()) {
                // Block block = GeoData.getInstance().getBlock(geoX, geoY);
                val geoZ = block.getHeightNearest(geoX, geoY, activeChar.z).toInt()
                val nswe = block.getNsweNearest(geoX, geoY, geoZ)

                // activeChar.sendMessage("NSWE: " + block.getClass().getSimpleName());
                activeChar.sendMessage("    " + (if ((nswe.toInt() and GeoStructure.CELL_FLAG_NW.toInt()) != 0) Y else N) + (if ((nswe.toInt() and GeoStructure.CELL_FLAG_N.toInt()) != 0) Y else N) + (if ((nswe.toInt() and GeoStructure.CELL_FLAG_NE.toInt()) != 0) Y else N) + "         GeoX=" + geoX)
                activeChar.sendMessage("    " + (if ((nswe.toInt() and GeoStructure.CELL_FLAG_W.toInt()) != 0) Y else N) + "o " + (if ((nswe.toInt() and GeoStructure.CELL_FLAG_E.toInt()) != 0) Y else N) + "         GeoY=" + geoY)
                activeChar.sendMessage("    " + (if ((nswe.toInt() and GeoStructure.CELL_FLAG_SW.toInt()) != 0) Y else N) + (if ((nswe.toInt() and GeoStructure.CELL_FLAG_S.toInt()) != 0) Y else N) + (if ((nswe.toInt() and GeoStructure.CELL_FLAG_SE.toInt()) != 0) Y else N) + "         GeoZ=" + geoZ)
            } else
                activeChar.sendMessage("There is no geodata at this position.")
        } else if (command == "admin_geo_see") {
            val target = activeChar.target
            if (target != null) {
                if (GeoEngine.canSeeTarget(activeChar, target))
                    activeChar.sendMessage("Can see target.")
                else
                    activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_SEE_TARGET))
            } else
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
        } else if (command == "admin_geo_move") {
            val target = activeChar.target
            if (target != null) {
                if (GeoEngine.canMoveToTarget(activeChar.x, activeChar.y, activeChar.z, target.x, target.y, target.z))
                    activeChar.sendMessage("Can move beeline.")
                else
                    activeChar.sendMessage("Can not move beeline!")
            } else
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
        } else if (command == "admin_path_find") {
            if (activeChar.target != null) {
                val path = GeoEngine.findPath(
                    activeChar.x,
                    activeChar.y,
                    activeChar.z.toShort().toInt(),
                    activeChar.target!!.x,
                    activeChar.target!!.y,
                    activeChar.target!!.z.toShort().toInt(),
                    true
                )
                if (path == null)
                    activeChar.sendMessage("No route found or pathfinding disabled.")
                else
                    for (point in path)
                        activeChar.sendMessage("x:" + point.x + " y:" + point.y + " z:" + point.z)
            } else
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
        } else if (command == "admin_path_info") {
            val info = GeoEngine.stat
            if (info.isEmpty())
                activeChar.sendMessage("Pathfinding disabled.")
            else
                for (msg in info) {
                    println(msg)
                    activeChar.sendMessage(msg)
                }
        } else
            return false

        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {

        private val ADMIN_COMMANDS = arrayOf(
            "admin_geo_bug",
            "admin_geo_pos",
            "admin_geo_see",
            "admin_geo_move",
            "admin_path_find",
            "admin_path_info"
        )
    }
}