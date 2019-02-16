package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.commons.lang.StringUtil
import com.l2kt.commons.math.MathUtil
import com.l2kt.gameserver.data.sql.BookmarkTable
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import java.util.*

/**
 * This class handles bookmarks (stored locations for GMs use).<br></br>
 * A bookmark is registered using //bk name. The book itself is called with //bk without parameter.
 * @author Tryskell
 */
class AdminBookmark : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (command.startsWith("admin_bkpage")) {
            val st = StringTokenizer(command, " ")
            st.nextToken() // skip command

            var page = 1
            if (st.hasMoreTokens())
                page = Integer.parseInt(st.nextToken())

            showBookmarks(activeChar, page)
        } else if (command.startsWith("admin_bk")) {
            val st = StringTokenizer(command, " ")
            st.nextToken() // skip command

            // Save the bookmark on SQL, and call the HTM.
            if (st.hasMoreTokens()) {
                val name = st.nextToken()

                if (name.length > 15) {
                    activeChar.sendMessage("The location name is too long.")
                    return true
                }

                if (BookmarkTable.isExisting(name, activeChar.objectId)) {
                    activeChar.sendMessage("That location is already existing.")
                    return true
                }

                BookmarkTable.saveBookmark(name, activeChar)
            }

            // Show the HTM.
            showBookmarks(activeChar, 1)
        } else if (command.startsWith("admin_delbk")) {
            val st = StringTokenizer(command, " ")
            st.nextToken() // skip command

            if (st.hasMoreTokens()) {
                val name = st.nextToken()
                val objId = activeChar.objectId

                if (!BookmarkTable.isExisting(name, objId)) {
                    activeChar.sendMessage("That location doesn't exist.")
                    return true
                }
                BookmarkTable.deleteBookmark(name, objId)
            } else
                activeChar.sendMessage("The command delbk must be followed by a valid name.")

            showBookmarks(activeChar, 1)
        }
        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val PAGE_LIMIT = 15

        private val ADMIN_COMMANDS = arrayOf("admin_bkpage", "admin_bk", "admin_delbk")

        /**
         * Show the basic HTM fed with generated data.
         * @param activeChar The player to make checks on.
         * @param page The page id to show.
         */
        private fun showBookmarks(activeChar: Player, page: Int) {
            val objId = activeChar.objectId
            var bookmarks = BookmarkTable.getBookmarks(objId)

            // Load static Htm.
            val html = NpcHtmlMessage(0)
            html.setFile("data/html/admin/bk.htm")

            if (bookmarks.isEmpty()) {
                html.replace("%locs%", "<tr><td>No bookmarks are currently registered.</td></tr>")
                activeChar.sendPacket(html)
                return
            }

            val max = MathUtil.countPagesNumber(bookmarks.size, PAGE_LIMIT)

            bookmarks = bookmarks.subList((page - 1) * PAGE_LIMIT, Math.min(page * PAGE_LIMIT, bookmarks.size))

            // Generate data.
            val sb = StringBuilder(2000)

            for ((name, _, x, y, z) in bookmarks) {

                StringUtil.append(
                    sb,
                    "<tr><td><a action=\"bypass -h admin_move_to ",
                    x,
                    " ",
                    y,
                    " ",
                    z,
                    "\">",
                    name,
                    " (",
                    x,
                    " ",
                    y,
                    " ",
                    z,
                    ")",
                    "</a></td><td><a action=\"bypass -h admin_delbk ",
                    name,
                    "\">Remove</a></td></tr>"
                )
            }
            html.replace("%locs%", sb.toString())

            // Cleanup the sb.
            sb.setLength(0)

            // End of table, open a new table for pages system.
            for (i in 0 until max) {
                val pagenr = i + 1
                if (page == pagenr)
                    StringUtil.append(sb, pagenr, "&nbsp;")
                else
                    StringUtil.append(sb, "<a action=\"bypass -h admin_bkpage ", pagenr, "\">", pagenr, "</a>&nbsp;")
            }

            html.replace("%pages%", sb.toString())
            activeChar.sendPacket(html)
        }
    }
}