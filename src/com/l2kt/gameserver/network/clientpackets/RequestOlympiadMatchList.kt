package com.l2kt.gameserver.network.clientpackets

import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.model.olympiad.Olympiad
import com.l2kt.gameserver.model.olympiad.OlympiadGameManager
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage

class RequestOlympiadMatchList : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val activeChar = client.activeChar
        if (activeChar == null || !activeChar.isInObserverMode)
            return

        val sb = StringBuilder(1500)
        for ((i, task) in OlympiadGameManager.getInstance().olympiadTasks.withIndex()) {
            StringUtil.append(
                sb,
                "<tr><td fixwidth=10><a action=\"bypass arenachange ",
                i,
                "\">",
                i + 1,
                "</a></td><td fixwidth=80>"
            )

            if (task.isGameStarted) {
                when {
                    task.isInTimerTime -> StringUtil.append(sb, "&$907;") // Counting In Progress
                    task.isBattleStarted -> StringUtil.append(sb, "&$829;") // In Progress
                    else -> StringUtil.append(sb, "&$908;")
                } // Terminate

                StringUtil.append(
                    sb,
                    "</td><td>",
                    task.game.playerNames[0],
                    "&nbsp; / &nbsp;",
                    task.game.playerNames[1]
                )
            } else
                StringUtil.append(sb, "&$906;", "</td><td>&nbsp;") // Initial State

            StringUtil.append(sb, "</td><td><font color=\"aaccff\"></font></td></tr>")
        }

        val html = NpcHtmlMessage(0)
        html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "olympiad_arena_observe_list.htm")
        html.replace("%list%", sb.toString())
        activeChar.sendPacket(html)
    }
}