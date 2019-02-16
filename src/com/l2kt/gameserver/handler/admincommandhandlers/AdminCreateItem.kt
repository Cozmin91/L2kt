package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.gameserver.data.ItemTable
import com.l2kt.gameserver.data.xml.ArmorSetData
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.ItemList
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import java.util.*

/**
 * This class handles following admin commands:<br></br>
 * <br></br>
 * - itemcreate = show "item creation" menu<br></br>
 * - create_item = creates num items with respective id, if num is not specified, assumes 1.<br></br>
 * - create_set = creates armorset with respective chest id.<br></br>
 * - create_coin = creates currency, using the choice box or typing good IDs.<br></br>
 * - reward_all = reward all online players with items.
 */
class AdminCreateItem : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        var command = command
        val st = StringTokenizer(command)
        command = st.nextToken()

        if (command == "admin_itemcreate") {
            AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm")
        } else if (command == "admin_reward_all") {
            try {
                val id = Integer.parseInt(st.nextToken())
                val count = if (st.hasMoreTokens()) Integer.parseInt(st.nextToken()) else 1

                val players = World.players
                for (player in players)
                    createItem(activeChar, player, id, count, 0, false)

                activeChar.sendMessage(players.size.toString() + " players rewarded with " + ItemTable.getTemplate(id)!!.name)
            } catch (e: Exception) {
                activeChar.sendMessage("Usage: //reward_all <itemId> [amount]")
            }

            AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm")
        } else {
            var target = activeChar
            if (activeChar.target != null && activeChar.target is Player)
                target = activeChar.target as Player

            if (command == "admin_create_item") {
                try {
                    val id = Integer.parseInt(st.nextToken())

                    var count = 1
                    var radius = 0

                    if (st.hasMoreTokens()) {
                        count = Integer.parseInt(st.nextToken())
                        if (st.hasMoreTokens())
                            radius = Integer.parseInt(st.nextToken())
                    }

                    createItem(activeChar, target, id, count, radius, true)
                } catch (e: Exception) {
                    activeChar.sendMessage("Usage: //create_item <itemId> [amount] [radius]")
                }

                AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm")
            } else if (command == "admin_create_coin") {
                try {
                    val id = getCoinId(st.nextToken())
                    if (id <= 0) {
                        activeChar.sendMessage("Usage: //create_coin <name> [amount]")
                        return false
                    }

                    createItem(
                        activeChar,
                        target,
                        id,
                        if (st.hasMoreTokens()) Integer.parseInt(st.nextToken()) else 1,
                        0,
                        true
                    )
                } catch (e: Exception) {
                    activeChar.sendMessage("Usage: //create_coin <name> [amount]")
                }

                AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm")
            } else if (command == "admin_create_set") {
                // More tokens means you try to use the command directly with a chestId.
                if (st.hasMoreTokens()) {
                    try {
                        val set = ArmorSetData.getSet(Integer.parseInt(st.nextToken()))
                        if (set == null) {
                            activeChar.sendMessage("This chest has no set.")
                            return false
                        }

                        for (itemId in set.setItemsId) {
                            if (itemId > 0)
                                target.inventory!!.addItem("Admin", itemId, 1, target, activeChar)
                        }

                        if (set.shield > 0)
                            target.inventory!!.addItem("Admin", set.shield, 1, target, activeChar)

                        activeChar.sendMessage("You have spawned " + set.toString() + " in " + target.name + "'s inventory.")

                        // Send the whole item list and open inventory window.
                        target.sendPacket(ItemList(target, true))
                    } catch (e: Exception) {
                        activeChar.sendMessage("Usage: //create_set <chestId>")
                    }

                }

                // Regular case (first HTM with all possible sets).
                var i = 0

                val sb = StringBuilder()
                for (set in ArmorSetData.sets) {
                    val isNextLine = i % 2 == 0
                    if (isNextLine)
                        sb.append("<tr>")

                    sb.append("<td><a action=\"bypass -h admin_create_set " + set.setItemsId[0] + "\">" + set.toString() + "</a></td>")

                    if (isNextLine)
                        sb.append("</tr>")

                    i++
                }

                val html = NpcHtmlMessage(0)
                html.setFile("data/html/admin/itemsets.htm")
                html.replace("%sets%", sb.toString())
                activeChar.sendPacket(html)
            }
        }
        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf(
            "admin_itemcreate",
            "admin_create_item",
            "admin_create_set",
            "admin_create_coin",
            "admin_reward_all"
        )

        private fun createItem(
            activeChar: Player,
            target: Player,
            id: Int,
            num: Int,
            radius: Int,
            sendGmMessage: Boolean
        ) {
            val template = ItemTable.getTemplate(id)
            if (template == null) {
                activeChar.sendMessage("This item doesn't exist.")
                return
            }

            if (num > 1 && !template.isStackable) {
                activeChar.sendMessage("This item doesn't stack - Creation aborted.")
                return
            }

            if (radius > 0) {
                val players = activeChar.getKnownTypeInRadius(Player::class.java, radius)
                for (obj in players) {
                    obj.addItem("Admin", id, num, activeChar, false)
                    obj.sendMessage("A GM spawned " + num + " " + template.name + " in your inventory.")
                }

                if (sendGmMessage)
                    activeChar.sendMessage(players.size.toString() + " players rewarded with " + num + " " + template.name + " in a " + radius + " radius.")
            } else {
                target.inventory!!.addItem("Admin", id, num, target, activeChar)
                if (activeChar != target)
                    target.sendMessage("A GM spawned " + num + " " + template.name + " in your inventory.")

                if (sendGmMessage)
                    activeChar.sendMessage("You have spawned " + num + " " + template.name + " (" + id + ") in " + target.name + "'s inventory.")

                // Send the whole item list and open inventory window.
                target.sendPacket(ItemList(target, true))
            }
        }

        private fun getCoinId(name: String): Int {
            if (name.equals("adena", ignoreCase = true))
                return 57

            if (name.equals("ancientadena", ignoreCase = true))
                return 5575

            return if (name.equals("festivaladena", ignoreCase = true)) 6673 else 0

        }
    }
}