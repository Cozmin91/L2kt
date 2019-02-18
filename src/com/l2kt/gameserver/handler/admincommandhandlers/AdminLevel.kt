package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.stat.PlayableStat
import com.l2kt.gameserver.model.base.Experience
import com.l2kt.gameserver.network.SystemMessageId
import java.util.*

class AdminLevel : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {

        val targetChar = activeChar.target

        val st = StringTokenizer(command, " ")
        val actualCommand = st.nextToken() // Get actual command

        var `val` = ""
        if (st.countTokens() >= 1)
            `val` = st.nextToken()

        if (actualCommand.equals("admin_addlevel", ignoreCase = true)) {
            try {
                if (targetChar is Playable)
                    (targetChar.stat as PlayableStat).addLevel(java.lang.Byte.parseByte(`val`))
            } catch (e: NumberFormatException) {
                activeChar.sendMessage("Wrong number format.")
                return false
            }

        } else if (actualCommand.equals("admin_setlevel", ignoreCase = true)) {
            try {
                if (targetChar == null || targetChar !is Player) {
                    activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT) // incorrect target!
                    return false
                }

                val lvl = java.lang.Byte.parseByte(`val`)
                if (lvl >= 1 && lvl <= Experience.MAX_LEVEL) {
                    val pXp = targetChar.exp
                    val tXp = Experience.LEVEL[lvl.toInt()]

                    if (pXp > tXp)
                        targetChar.removeExpAndSp(pXp - tXp, 0)
                    else if (pXp < tXp)
                        targetChar.addExpAndSp(tXp - pXp, 0)
                } else {
                    activeChar.sendMessage("You must specify level between 1 and " + Experience.MAX_LEVEL + ".")
                    return false
                }
            } catch (e: NumberFormatException) {
                activeChar.sendMessage("You must specify level between 1 and " + Experience.MAX_LEVEL + ".")
                return false
            }

        }
        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf("admin_addlevel", "admin_setlevel")
    }
}