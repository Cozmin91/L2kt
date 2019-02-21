package com.l2kt.gameserver.model.actor.instance

import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.data.manager.DerbyTrackManager
import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.DeleteObject
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.util.*

class DerbyTrackManagerNpc(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {

    override fun onBypassFeedback(player: Player, command: String) {
        if (command.startsWith("BuyTicket")) {
            if (DerbyTrackManager.currentRaceState !== DerbyTrackManager.RaceState.ACCEPTING_BETS) {
                player.sendPacket(SystemMessageId.MONSRACE_TICKETS_NOT_AVAILABLE)
                super.onBypassFeedback(player, "Chat 0")
                return
            }

            var `val` = Integer.parseInt(command.substring(10))
            if (`val` == 0) {
                player.setRace(0, 0)
                player.setRace(1, 0)
            }

            if (`val` == 10 && player.getRace(0) == 0 || `val` == 20 && player.getRace(0) == 0 && player.getRace(1) == 0)
                `val` = 0

            val npcId = template.npcId
            var search: String
            val replace: String

            val html = NpcHtmlMessage(objectId)

            if (`val` < 10) {
                html.setFile(getHtmlPath(npcId, 2))
                for (i in 0..7) {
                    val n = i + 1
                    search = "Mob$n"
                    html.replace(search, DerbyTrackManager.getRunnerName(i))
                }
                search = "No1"
                if (`val` == 0)
                    html.replace(search, "")
                else {
                    html.replace(search, `val`)
                    player.setRace(0, `val`)
                }
            } else if (`val` < 20) {
                if (player.getRace(0) == 0)
                    return

                html.setFile(getHtmlPath(npcId, 3))
                html.replace("0place", player.getRace(0))
                search = "Mob1"
                replace = DerbyTrackManager.getRunnerName(player.getRace(0) - 1)
                html.replace(search, replace)
                search = "0adena"

                if (`val` == 10)
                    html.replace(search, "")
                else {
                    html.replace(search, TICKET_PRICES[`val` - 11])
                    player.setRace(1, `val` - 10)
                }
            } else if (`val` == 20) {
                if (player.getRace(0) == 0 || player.getRace(1) == 0)
                    return

                html.setFile(getHtmlPath(npcId, 4))
                html.replace("0place", player.getRace(0))
                search = "Mob1"
                replace = DerbyTrackManager.getRunnerName(player.getRace(0) - 1)
                html.replace(search, replace)
                search = "0adena"
                val price = TICKET_PRICES[player.getRace(1) - 1]
                html.replace(search, price)
                search = "0tax"
                val tax = 0
                html.replace(search, tax)
                search = "0total"
                val total = price + tax
                html.replace(search, total)
            } else {
                if (player.getRace(0) == 0 || player.getRace(1) == 0)
                    return

                val ticket = player.getRace(0)
                val priceId = player.getRace(1)

                if (!player.reduceAdena("Race", TICKET_PRICES[priceId - 1], this, true))
                    return

                player.setRace(0, 0)
                player.setRace(1, 0)

                val item = ItemInstance(IdFactory.getInstance().nextId, 4443)
                item.count = 1
                item.enchantLevel = DerbyTrackManager.raceNumber
                item.customType1 = ticket
                item.customType2 = TICKET_PRICES[priceId - 1] / 100

                player.addItem("Race", item, player, false)
                player.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_S2).addNumber(
                        DerbyTrackManager.raceNumber
                    ).addItemName(4443)
                )

                // Refresh lane bet.
                DerbyTrackManager.setBetOnLane(ticket, TICKET_PRICES[priceId - 1].toLong(), true)
                super.onBypassFeedback(player, "Chat 0")
                return
            }
            html.replace("1race", DerbyTrackManager.raceNumber)
            html.replace("%objectId%", objectId)
            player.sendPacket(html)
            player.sendPacket(ActionFailed.STATIC_PACKET)
        } else if (command == "ShowOdds") {
            if (DerbyTrackManager.currentRaceState === DerbyTrackManager.RaceState.ACCEPTING_BETS) {
                player.sendPacket(SystemMessageId.MONSRACE_NO_PAYOUT_INFO)
                super.onBypassFeedback(player, "Chat 0")
                return
            }

            val html = NpcHtmlMessage(objectId)
            html.setFile(getHtmlPath(template.npcId, 5))
            for (i in 0..7) {
                val n = i + 1

                html.replace("Mob$n", DerbyTrackManager.getRunnerName(i))

                // Odd
                val odd = DerbyTrackManager.odds[i]
                html.replace("Odd$n", if (odd > 0.0) String.format(Locale.ENGLISH, "%.1f", odd) else "&$804;")
            }
            html.replace("1race", DerbyTrackManager.raceNumber)
            html.replace("%objectId%", objectId)
            player.sendPacket(html)
            player.sendPacket(ActionFailed.STATIC_PACKET)
        } else if (command == "ShowInfo") {
            val html = NpcHtmlMessage(objectId)
            html.setFile(getHtmlPath(template.npcId, 6))

            for (i in 0..7) {
                val n = i + 1
                val search = "Mob$n"
                html.replace(search, DerbyTrackManager.getRunnerName(i))
            }
            html.replace("%objectId%", objectId)
            player.sendPacket(html)
            player.sendPacket(ActionFailed.STATIC_PACKET)
        } else if (command == "ShowTickets") {
            // Generate data.
            val sb = StringBuilder()

            // Retrieve player's tickets.
            for (ticket in player.inventory!!.getAllItemsByItemId(4443)) {
                // Don't list current race tickets.
                if (ticket.enchantLevel == DerbyTrackManager.raceNumber)
                    continue

                StringUtil.append(
                    sb,
                    "<tr><td><a action=\"bypass -h npc_%objectId%_ShowTicket ",
                    ticket.objectId,
                    "\">",
                    ticket.enchantLevel,
                    " Race Number</a></td><td align=right><font color=\"LEVEL\">",
                    ticket.customType1,
                    "</font> Number</td><td align=right><font color=\"LEVEL\">",
                    ticket.customType2 * 100,
                    "</font> Adena</td></tr>"
                )
            }

            val html = NpcHtmlMessage(objectId)
            html.setFile(getHtmlPath(template.npcId, 7))
            html.replace("%tickets%", sb.toString())
            html.replace("%objectId%", objectId)
            player.sendPacket(html)
            player.sendPacket(ActionFailed.STATIC_PACKET)
        } else if (command.startsWith("ShowTicket")) {
            // Retrieve ticket objectId.
            val `val` = Integer.parseInt(command.substring(11))
            if (`val` == 0) {
                super.onBypassFeedback(player, "Chat 0")
                return
            }

            // Retrieve ticket on player's inventory.
            val ticket = player.inventory!!.getItemByObjectId(`val`)
            if (ticket == null) {
                super.onBypassFeedback(player, "Chat 0")
                return
            }

            val raceId = ticket.enchantLevel
            val lane = ticket.customType1
            val bet = ticket.customType2 * 100

            // Retrieve HistoryInfo for that race.
            val info = DerbyTrackManager.getHistoryInfo(raceId)
            if (info == null) {
                super.onBypassFeedback(player, "Chat 0")
                return
            }

            val html = NpcHtmlMessage(objectId)
            html.setFile(getHtmlPath(template.npcId, 8))
            html.replace("%raceId%", raceId)
            html.replace("%lane%", lane)
            html.replace("%bet%", bet)
            html.replace("%firstLane%", info.first + 1)
            html.replace(
                "%odd%",
                if (lane == info.first + 1) String.format(Locale.ENGLISH, "%.2f", info.oddRate) else "0.01"
            )
            html.replace("%objectId%", objectId)
            html.replace("%ticketObjectId%", `val`)
            player.sendPacket(html)
            player.sendPacket(ActionFailed.STATIC_PACKET)
        } else if (command.startsWith("CalculateWin")) {
            // Retrieve ticket objectId.
            val `val` = Integer.parseInt(command.substring(13))
            if (`val` == 0) {
                super.onBypassFeedback(player, "Chat 0")
                return
            }

            // Delete ticket on player's inventory.
            val ticket = player.inventory!!.getItemByObjectId(`val`)
            if (ticket == null) {
                super.onBypassFeedback(player, "Chat 0")
                return
            }

            val raceId = ticket.enchantLevel
            val lane = ticket.customType1
            val bet = ticket.customType2 * 100

            // Retrieve HistoryInfo for that race.
            val info = DerbyTrackManager.getHistoryInfo(raceId)
            if (info == null) {
                super.onBypassFeedback(player, "Chat 0")
                return
            }

            // Destroy the ticket.
            if (player.destroyItem("MonsterTrack", ticket, this, true))
                player.addAdena(
                    "MonsterTrack",
                    (bet * if (lane == info.first + 1) info.oddRate else 0.01).toInt(),
                    this,
                    true
                )

            super.onBypassFeedback(player, "Chat 0")
            return
        } else if (command == "ViewHistory") {
            // Generate data.
            val sb = StringBuilder()

            // Retrieve current race number.
            val raceNumber = DerbyTrackManager.raceNumber

            // Retrieve the few latest entries.
            for ((raceId, first, second, oddRate) in DerbyTrackManager.lastHistoryEntries)
                StringUtil.append(
                    sb,
                    "<tr><td><font color=\"LEVEL\">",
                    raceId,
                    "</font> th</td><td><font color=\"LEVEL\">",
                    if (raceNumber == raceId) 0 else first + 1,
                    "</font> Lane </td><td><font color=\"LEVEL\">",
                    if (raceNumber == raceId) 0 else second + 1,
                    "</font> Lane</td><td align=right><font color=00ffff>",
                    String.format(Locale.ENGLISH, "%.2f", oddRate),
                    "</font> Times</td></tr>"
                )

            val html = NpcHtmlMessage(objectId)
            html.setFile(getHtmlPath(template.npcId, 9))
            html.replace("%infos%", sb.toString())
            html.replace("%objectId%", objectId)
            player.sendPacket(html)
            player.sendPacket(ActionFailed.STATIC_PACKET)
        } else
            super.onBypassFeedback(player, command)
    }

    override fun addKnownObject(`object`: WorldObject) {
        if (`object` is Player)
            `object`.sendPacket(DerbyTrackManager.racePacket)
    }

    override fun removeKnownObject(`object`: WorldObject) {
        super.removeKnownObject(`object`)

        if (`object` is Player) {

            for (npc in DerbyTrackManager.runners!!)
                `object`.sendPacket(DeleteObject(npc))
        }
    }

    companion object {
        protected val TICKET_PRICES = intArrayOf(100, 500, 1000, 5000, 10000, 20000, 50000, 100000)
    }
}