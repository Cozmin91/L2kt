package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.instancemanager.AutoSpawnManager
import com.l2kt.gameserver.instancemanager.SevenSigns
import com.l2kt.gameserver.model.actor.instance.Player

/**
 * Admin Command Handler for Mammon NPCs
 * @author Tempy
 */
class AdminMammon : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (command.startsWith("admin_mammon_find")) {
            var teleportIndex = -1

            try {
                teleportIndex = Integer.parseInt(command.substring(18))
            } catch (NumberFormatException: Exception) {
                activeChar.sendMessage("Usage: //mammon_find [teleportIndex] (1 / 2)")
                return false
            }

            if (!SevenSigns.isSealValidationPeriod) {
                activeChar.sendMessage("The competition period is currently in effect.")
                return true
            }

            if (teleportIndex == 1) {
                val blackSpawnInst =
                    AutoSpawnManager.getInstance().getAutoSpawnInstance(SevenSigns.MAMMON_BLACKSMITH_ID, false)
                if (blackSpawnInst != null) {
                    val blackInst = blackSpawnInst.npcInstanceList
                    if (blackInst.size > 0) {
                        val x1 = blackInst[0].x
                        val y1 = blackInst[0].y
                        val z1 = blackInst[0].z
                        activeChar.sendMessage("Blacksmith of Mammon: $x1 $y1 $z1")
                        activeChar.teleToLocation(x1, y1, z1, 0)
                    }
                } else
                    activeChar.sendMessage("Blacksmith of Mammon isn't registered.")
            } else if (teleportIndex == 2) {
                val merchSpawnInst =
                    AutoSpawnManager.getInstance().getAutoSpawnInstance(SevenSigns.MAMMON_MERCHANT_ID, false)
                if (merchSpawnInst != null) {
                    val merchInst = merchSpawnInst.npcInstanceList
                    if (merchInst.size > 0) {
                        val x2 = merchInst[0].x
                        val y2 = merchInst[0].y
                        val z2 = merchInst[0].z
                        activeChar.sendMessage("Merchant of Mammon: $x2 $y2 $z2")
                        activeChar.teleToLocation(x2, y2, z2, 0)
                    }
                } else
                    activeChar.sendMessage("Merchant of Mammon isn't registered.")
            } else
                activeChar.sendMessage("Invalid parameter '$teleportIndex' for //mammon_find.")
        } else if (command.startsWith("admin_mammon_respawn")) {
            if (!SevenSigns.isSealValidationPeriod) {
                activeChar.sendMessage("The competition period is currently in effect.")
                return true
            }

            val merchSpawnInst =
                AutoSpawnManager.getInstance().getAutoSpawnInstance(SevenSigns.MAMMON_MERCHANT_ID, false)
            if (merchSpawnInst != null) {
                val merchRespawn = AutoSpawnManager.getInstance().getTimeToNextSpawn(merchSpawnInst)
                activeChar.sendMessage("The Merchant of Mammon will respawn in " + merchRespawn / 60000 + " minute(s).")
            } else
                activeChar.sendMessage("Merchant of Mammon isn't registered.")

            val blackSpawnInst =
                AutoSpawnManager.getInstance().getAutoSpawnInstance(SevenSigns.MAMMON_BLACKSMITH_ID, false)
            if (blackSpawnInst != null) {
                val blackRespawn = AutoSpawnManager.getInstance().getTimeToNextSpawn(blackSpawnInst)
                activeChar.sendMessage("The Blacksmith of Mammon will respawn in " + blackRespawn / 60000 + " minute(s).")
            } else
                activeChar.sendMessage("Blacksmith of Mammon isn't registered.")
        }

        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf("admin_mammon_find", "admin_mammon_respawn")
    }
}