package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.data.manager.CastleManorManager
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage

class AdminManor : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (command.startsWith("admin_manor")) {
            val manor = CastleManorManager

            val msg = NpcHtmlMessage(0)
            msg.setFile("data/html/admin/manor.htm")
            msg.replace("%status%", manor.currentModeName)
            msg.replace("%change%", manor.nextModeChange)

            val sb = StringBuilder(3400)
            for (c in CastleManager.castles) {
                StringUtil.append(
                    sb,
                    "<tr><td width=110>Name:</td><td width=160><font color=008000>" + c.name + "</font></td></tr>"
                )
                StringUtil.append(
                    sb,
                    "<tr><td>Current period cost:</td><td><font color=FF9900>",
                    StringUtil.formatNumber(manor.getManorCost(c.castleId, false)),
                    " Adena</font></td></tr>"
                )
                StringUtil.append(
                    sb,
                    "<tr><td>Next period cost:</td><td><font color=FF9900>",
                    StringUtil.formatNumber(manor.getManorCost(c.castleId, true)),
                    " Adena</font></td></tr>"
                )
                StringUtil.append(sb, "<tr><td>&nbsp;</td></tr>")
            }
            msg.replace("%castleInfo%", sb.toString())
            activeChar.sendPacket(msg)

            sb.setLength(0)
        }

        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf("admin_manor")
    }
}