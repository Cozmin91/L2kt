package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.actor.instance.Player
import java.util.*

/**
 * This class handles following admin commands:
 *
 *  * gm = turns gm mode off for a short period of time (by default 1 minute).
 *
 */
class AdminGm : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (command.startsWith("admin_gm")) {
            val st = StringTokenizer(command, " ")
            st.nextToken()

            var numberOfMinutes = 1
            if (st.hasMoreTokens()) {
                try {
                    numberOfMinutes = Integer.parseInt(st.nextToken())
                } catch (e: Exception) {
                    activeChar.sendMessage("Invalid timer setted for //gm ; default time is used.")
                }

            }

            // We keep the previous level to rehabilitate it later.
            val previousAccessLevel = activeChar.accessLevel.level

            activeChar.setAccessLevel(0)
            activeChar.sendMessage("You no longer have GM status, but will be rehabilitated after $numberOfMinutes minutes.")

            ThreadPool.schedule(Runnable{
                if (!activeChar.isOnline)
                    return@Runnable

                activeChar.setAccessLevel(previousAccessLevel)
                activeChar.sendMessage("Your previous access level has been rehabilitated.")
            }, (numberOfMinutes * 60000).toLong())
        }
        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf("admin_gm")
    }
}