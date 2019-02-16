package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.commons.lang.StringUtil
import com.l2kt.commons.math.MathUtil
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import java.util.*

/**
 * Handles visibility over target's knownlist, offering details about current target's vicinity.
 */
class AdminKnownlist : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (command.startsWith("admin_knownlist")) {
            val st = StringTokenizer(command, " ")
            st.nextToken()

            var target: WorldObject? = null

            // Try to parse the parameter as an int, then try to retrieve an objectId ; if it's a string, search for any player name.
            if (st.hasMoreTokens()) {
                val parameter = st.nextToken()

                try {
                    val objectId = Integer.parseInt(parameter)
                    target = World.getObject(objectId)
                } catch (nfe: NumberFormatException) {
                    target = World.getPlayer(parameter)
                }

            }

            // If no one is found, pick potential activeChar's target or the activeChar himself.
            if (target == null) {
                target = activeChar.target
                if (target == null)
                    target = activeChar
            }

            var page = 1

            if (command.startsWith("admin_knownlist_page") && st.hasMoreTokens()) {
                try {
                    page = Integer.parseInt(st.nextToken())
                } catch (nfe: NumberFormatException) {
                }

            }

            showKnownlist(activeChar, target, page)
        }
        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val PAGE_LIMIT = 15

        private val ADMIN_COMMANDS = arrayOf("admin_knownlist", "admin_knownlist_page")

        private fun showKnownlist(activeChar: Player, target: WorldObject, page: Int) {
            var page = page
            var knownlist = target.getKnownType(WorldObject::class.java)

            // Load static Htm.
            val html = NpcHtmlMessage(0)
            html.setFile("data/html/admin/knownlist.htm")
            html.replace("%target%", target.name)
            html.replace("%size%", knownlist.size)

            if (knownlist.isEmpty()) {
                html.replace("%knownlist%", "<tr><td>No objects in vicinity.</td></tr>")
                html.replace("%pages%", 0)
                activeChar.sendPacket(html)
                return
            }

            val max = MathUtil.countPagesNumber(knownlist.size, PAGE_LIMIT)
            if (page > max)
                page = max

            knownlist = knownlist.subList((page - 1) * PAGE_LIMIT, Math.min(page * PAGE_LIMIT, knownlist.size))

            // Generate data.
            val sb = StringBuilder(knownlist.size * 150)
            for (`object` in knownlist)
                StringUtil.append(
                    sb,
                    "<tr><td>",
                    `object`.name,
                    "</td><td>",
                    `object`.javaClass.simpleName,
                    "</td></tr>"
                )

            html.replace("%knownlist%", sb.toString())

            sb.setLength(0)

            // End of table, open a new table for pages system.
            for (i in 0 until max) {
                val pagenr = i + 1
                if (page == pagenr)
                    StringUtil.append(sb, pagenr, "&nbsp;")
                else
                    StringUtil.append(
                        sb,
                        "<a action=\"bypass -h admin_knownlist_page ",
                        target.objectId,
                        " ",
                        pagenr,
                        "\">",
                        pagenr,
                        "</a>&nbsp;"
                    )
            }

            html.replace("%pages%", sb.toString())

            activeChar.sendPacket(html)
        }
    }
}