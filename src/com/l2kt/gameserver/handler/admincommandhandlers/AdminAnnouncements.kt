package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.gameserver.data.xml.AnnouncementData
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.instance.Player

/**
 * This class handles following admin commands:
 *
 *  * announce list|all|all_auto|add|add_auto|del : announcement management.
 *  * ann : announces to all players (basic usage).
 *  * say : critical announces to all players.
 *
 */
class AdminAnnouncements : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (command.startsWith("admin_announce")) {
            try {
                val tokens = command.split(" ".toRegex(), 3).toTypedArray()
                when (tokens[1]) {
                    "list" -> AnnouncementData.listAnnouncements(activeChar)

                    "all", "all_auto" -> {
                        val isAuto = tokens[1].equals("all_auto", ignoreCase = true)
                        for (player in World.players)
                            AnnouncementData.showAnnouncements(player, isAuto)

                        AnnouncementData.listAnnouncements(activeChar)
                    }

                    "add" -> {
                        val split = tokens[2].split(" ".toRegex(), 2).toTypedArray() // boolean string
                        val crit = java.lang.Boolean.parseBoolean(split[0])

                        if (!AnnouncementData.addAnnouncement(split[1], crit, false, -1, -1, -1))
                            activeChar.sendMessage("Invalid //announce message content ; can't be null or empty.")

                        AnnouncementData.listAnnouncements(activeChar)
                    }

                    "add_auto" -> {
                        val split = tokens[2].split(" ".toRegex(), 6).toTypedArray() // boolean boolean int int int string
                        val crit = java.lang.Boolean.parseBoolean(split[0])
                        val auto = java.lang.Boolean.parseBoolean(split[1])
                        val idelay = Integer.parseInt(split[2])
                        val delay = Integer.parseInt(split[3])
                        val limit = Integer.parseInt(split[4])
                        val msg = split[5]

                        if (!AnnouncementData.addAnnouncement(msg, crit, auto, idelay, delay, limit))
                            activeChar.sendMessage("Invalid //announce message content ; can't be null or empty.")

                        AnnouncementData.listAnnouncements(activeChar)
                    }

                    "del" -> {
                        AnnouncementData.delAnnouncement(Integer.parseInt(tokens[2]))
                        AnnouncementData.listAnnouncements(activeChar)
                    }

                    else -> activeChar.sendMessage("Possible //announce parameters : <list|all|add|add_auto|del>")
                }
            } catch (e: Exception) {
                activeChar.sendMessage("Possible //announce parameters : <list|all|add|add_auto|del>")
            }

        } else if (command.startsWith("admin_ann") || command.startsWith("admin_say"))
            AnnouncementData.handleAnnounce(command, 10, command.startsWith("admin_say"))

        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf("admin_announce", "admin_ann", "admin_say")
    }
}