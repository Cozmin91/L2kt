package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.template.PlayerTemplate
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import java.util.*

/**
 * This class handles following admin commands:
 *  * add_exp_sp_to_character *shows menu for add or remove*
 *  * add_exp_sp exp sp *Adds exp & sp to target, displays menu if a parameter is missing*
 *  * remove_exp_sp exp sp *Removes exp & sp from target, displays menu if a parameter is missing*
 */
class AdminExpSp : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (command.startsWith("admin_add_exp_sp")) {
            try {
                val `val` = command.substring(16)
                if (!adminAddExpSp(activeChar, `val`))
                    activeChar.sendMessage("Usage: //add_exp_sp exp sp")
            } catch (e: StringIndexOutOfBoundsException) { // Case of missing parameter
                activeChar.sendMessage("Usage: //add_exp_sp exp sp")
            }

        } else if (command.startsWith("admin_remove_exp_sp")) {
            try {
                val `val` = command.substring(19)
                if (!adminRemoveExpSP(activeChar, `val`))
                    activeChar.sendMessage("Usage: //remove_exp_sp exp sp")
            } catch (e: StringIndexOutOfBoundsException) { // Case of missing parameter
                activeChar.sendMessage("Usage: //remove_exp_sp exp sp")
            }

        }
        addExpSp(activeChar)
        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf("admin_add_exp_sp_to_character", "admin_add_exp_sp", "admin_remove_exp_sp")

        private fun addExpSp(activeChar: Player) {
            val target = activeChar.target
            var player: Player? = null
            if (target is Player)
                player = target
            else {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
                return
            }
            val html = NpcHtmlMessage(0)
            html.setFile("data/html/admin/expsp.htm")
            html.replace("%name%", player.name)
            html.replace("%level%", player.level)
            html.replace("%xp%", player.exp)
            html.replace("%sp%", player.sp)
            html.replace("%class%", (player.template as PlayerTemplate).className)
            activeChar.sendPacket(html)
        }

        private fun adminAddExpSp(activeChar: Player, ExpSp: String): Boolean {
            val target = activeChar.target
            var player: Player? = null
            if (target is Player) {
                player = target
            } else {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
                return false
            }
            val st = StringTokenizer(ExpSp)
            if (st.countTokens() != 2)
                return false

            val exp = st.nextToken()
            val sp = st.nextToken()
            var expval: Long = 0
            var spval = 0
            try {
                expval = java.lang.Long.parseLong(exp)
                spval = Integer.parseInt(sp)
            } catch (e: Exception) {
                return false
            }

            if (expval != 0L || spval != 0) {
                player.sendMessage("Admin is adding you $expval xp and $spval sp.")
                player.addExpAndSp(expval, spval)

                activeChar.sendMessage("Added " + expval + " xp and " + spval + " sp to " + player.name + ".")
            }
            return true
        }

        private fun adminRemoveExpSP(activeChar: Player, ExpSp: String): Boolean {
            val target = activeChar.target
            var player: Player? = null
            if (target is Player) {
                player = target
            } else {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
                return false
            }
            val st = StringTokenizer(ExpSp)
            if (st.countTokens() != 2)
                return false

            val exp = st.nextToken()
            val sp = st.nextToken()
            var expval: Long = 0
            var spval = 0
            try {
                expval = java.lang.Long.parseLong(exp)
                spval = Integer.parseInt(sp)
            } catch (e: Exception) {
                return false
            }

            if (expval != 0L || spval != 0) {
                player.sendMessage("Admin is removing you $expval xp and $spval sp.")
                player.removeExpAndSp(expval, spval)

                activeChar.sendMessage("Removed " + expval + " xp and " + spval + " sp from " + player.name + ".")
            }
            return true
        }
    }
}