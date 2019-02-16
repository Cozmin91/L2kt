package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.olympiad.Olympiad

/**
 * This class handles following admin commands:
 *
 *  * endoly : ends olympiads manually.
 *  * sethero : set the target as a temporary hero.
 *  * setnoble : set the target as a noble.
 *
 */
class AdminOlympiad : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (command.startsWith("admin_endoly")) {
            Olympiad.getInstance().manualSelectHeroes()
            activeChar.sendMessage("Heroes have been formed.")
        } else if (command.startsWith("admin_sethero")) {
            var target: Player? = null
            if (activeChar.target is Player)
                target = activeChar.target as Player
            else
                target = activeChar

            target.isHero = !target.isHero
            target.broadcastUserInfo()
            activeChar.sendMessage("You have modified " + target.name + "'s hero status.")
        } else if (command.startsWith("admin_setnoble")) {
            var target: Player? = null
            if (activeChar.target is Player)
                target = activeChar.target as Player
            else
                target = activeChar

            target.setNoble(!target.isNoble, true)
            activeChar.sendMessage("You have modified " + target.name + "'s noble status.")
        }

        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf("admin_endoly", "admin_sethero", "admin_setnoble")
    }
}