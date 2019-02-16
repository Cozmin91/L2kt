package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.Config
import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.data.ItemTable
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.cache.CrestCache
import com.l2kt.gameserver.data.cache.HtmCache
import com.l2kt.gameserver.data.manager.CursedWeaponManager
import com.l2kt.gameserver.data.manager.ZoneManager
import com.l2kt.gameserver.data.xml.*
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import java.util.*

/**
 * This class handles following admin commands:
 *
 *  * admin/admin1/admin2/admin3/admin4 : the different admin menus.
 *  * gmlist : includes/excludes active character from /gmlist results.
 *  * kill : handles the kill command.
 *  * silence : toggles private messages acceptance mode.
 *  * tradeoff : toggles trade acceptance mode.
 *  * reload : reloads specified component.
 *  * script_load : loads following script. MUSTN'T be used instead of //reload quest !
 *
 */
class AdminAdmin : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (command.startsWith("admin_admin"))
            showMainPage(activeChar, command)
        else if (command.startsWith("admin_gmlist"))
            activeChar.sendMessage(if (AdminData.showOrHideGm(activeChar)) "Removed from GMList." else "Registered into GMList.")
        else if (command.startsWith("admin_kill")) {
            val st = StringTokenizer(command, " ")
            st.nextToken() // skip command

            if (!st.hasMoreTokens()) {
                val obj = activeChar.target
                if (obj !is Creature)
                    activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
                else
                    kill(activeChar, obj)

                return true
            }

            val firstParam = st.nextToken()
            val player = World.getPlayer(firstParam)
            if (player != null) {
                if (st.hasMoreTokens()) {
                    val secondParam = st.nextToken()
                    if (StringUtil.isDigit(secondParam)) {
                        val radius = Integer.parseInt(secondParam)
                        for (knownChar in player.getKnownTypeInRadius(Creature::class.java, radius)) {
                            if (knownChar == activeChar)
                                continue

                            kill(activeChar, knownChar)
                        }
                        activeChar.sendMessage("Killed all characters within a " + radius + " unit radius around " + player.name + ".")
                    } else
                        activeChar.sendMessage("Invalid radius.")
                } else
                    kill(activeChar, player)
            } else if (StringUtil.isDigit(firstParam)) {
                val radius = Integer.parseInt(firstParam)
                for (knownChar in activeChar.getKnownTypeInRadius(Creature::class.java, radius))
                    kill(activeChar, knownChar)

                activeChar.sendMessage("Killed all characters within a $radius unit radius.")
            }
        } else if (command.startsWith("admin_silence")) {
            if (activeChar.isInRefusalMode)
            // already in message refusal mode
            {
                activeChar.isInRefusalMode = false
                activeChar.sendPacket(SystemMessageId.MESSAGE_ACCEPTANCE_MODE)
            } else {
                activeChar.isInRefusalMode = true
                activeChar.sendPacket(SystemMessageId.MESSAGE_REFUSAL_MODE)
            }
        } else if (command.startsWith("admin_tradeoff")) {
            try {
                val mode = command.substring(15)
                if (mode.equals("on", ignoreCase = true)) {
                    activeChar.tradeRefusal = true
                    activeChar.sendMessage("Trade refusal enabled")
                } else if (mode.equals("off", ignoreCase = true)) {
                    activeChar.tradeRefusal = false
                    activeChar.sendMessage("Trade refusal disabled")
                }
            } catch (e: Exception) {
                if (activeChar.tradeRefusal) {
                    activeChar.tradeRefusal = false
                    activeChar.sendMessage("Trade refusal disabled")
                } else {
                    activeChar.tradeRefusal = true
                    activeChar.sendMessage("Trade refusal enabled")
                }
            }

        } else if (command.startsWith("admin_reload")) {
            val st = StringTokenizer(command)
            st.nextToken()
            try {
                do {
                    val type = st.nextToken()
                    if (type.startsWith("admin")) {
                        AdminData.reload()
                        activeChar.sendMessage("Admin data has been reloaded.")
                    } else if (type.startsWith("announcement")) {
                        AnnouncementData.reload()
                        activeChar.sendMessage("The content of announcements.xml has been reloaded.")
                    } else if (type.startsWith("config")) {
                        Config.loadGameServer()
                        activeChar.sendMessage("Configs files have been reloaded.")
                    } else if (type.startsWith("crest")) {
                        CrestCache.reload()
                        activeChar.sendMessage("Crests have been reloaded.")
                    } else if (type.startsWith("cw")) {
                        CursedWeaponManager.reload()
                        activeChar.sendMessage("Cursed weapons have been reloaded.")
                    } else if (type.startsWith("door")) {
                        DoorData.reload()
                        activeChar.sendMessage("Doors instance has been reloaded.")
                    } else if (type.startsWith("htm")) {
                        HtmCache.reload()
                        activeChar.sendMessage("The HTM cache has been reloaded.")
                    } else if (type.startsWith("item")) {
                        ItemTable.reload()
                        activeChar.sendMessage("Items' templates have been reloaded.")
                    } else if (type == "multisell") {
                        MultisellData.reload()
                        activeChar.sendMessage("The multisell instance has been reloaded.")
                    } else if (type == "npc") {
                        NpcData.reload()
                        activeChar.sendMessage("NPCs templates have been reloaded.")
                    } else if (type.startsWith("npcwalker")) {
                        WalkerRouteData.reload()
                        activeChar.sendMessage("Walker routes have been reloaded.")
                    } else if (type.startsWith("skill")) {
                        SkillTable.reload()
                        activeChar.sendMessage("Skills' XMLs have been reloaded.")
                    } else if (type.startsWith("teleport")) {
                        TeleportLocationData.reload()
                        activeChar.sendMessage("Teleport locations have been reloaded.")
                    } else if (type.startsWith("zone")) {
                        ZoneManager.reload()
                        activeChar.sendMessage("Zones have been reloaded.")
                    } else {
                        activeChar.sendMessage("Usage : //reload <admin|announcement|config|crest|cw>")
                        activeChar.sendMessage("Usage : //reload <door|htm|item|multisell|npc>")
                        activeChar.sendMessage("Usage : //reload <npcwalker|skill|teleport|zone>")
                    }
                } while (st.hasMoreTokens())
            } catch (e: Exception) {
                activeChar.sendMessage("Usage : //reload <admin|announcement|config|crest|cw>")
                activeChar.sendMessage("Usage : //reload <door|htm|item|multisell|npc>")
                activeChar.sendMessage("Usage : //reload <npcwalker|skill|teleport|zone>")
            }

        }
        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf(
            "admin_admin",
            "admin_admin1",
            "admin_admin2",
            "admin_admin3",
            "admin_admin4",
            "admin_gmlist",
            "admin_kill",
            "admin_silence",
            "admin_tradeoff",
            "admin_reload"
        )

        private fun kill(activeChar: Player, target: Creature) {
            if (target is Player) {
                if (!target.isGM)
                    target.stopAllEffects() // e.g. invincibility effect
                target.reduceCurrentHp((target.getMaxHp() + target.getMaxCp() + 1).toDouble(), activeChar, null)
            } else if (target.isChampion)
                target.reduceCurrentHp((target.maxHp * Config.CHAMPION_HP + 1).toDouble(), activeChar, null)
            else
                target.reduceCurrentHp((target.maxHp + 1).toDouble(), activeChar, null)
        }

        private fun showMainPage(activeChar: Player, command: String) {
            var mode = 0
            var filename: String? = null
            try {
                mode = Integer.parseInt(command.substring(11))
            } catch (e: Exception) {
            }

            when (mode) {
                1 -> filename = "main"
                2 -> filename = "game"
                3 -> filename = "effects"
                4 -> filename = "server"
                else -> filename = "main"
            }
            AdminHelpPage.showHelpPage(activeChar, filename + "_menu.htm")
        }
    }
}