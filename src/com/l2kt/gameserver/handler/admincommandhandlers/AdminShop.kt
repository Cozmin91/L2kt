package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.gameserver.data.manager.BuyListManager
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.BuyList

/**
 * This class handles following admin commands:
 *
 *  * gmshop = shows menu
 *  * buy id = shows shop with respective id
 *
 */
class AdminShop : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (command.startsWith("admin_buy")) {
            try {
                val `val` = Integer.parseInt(command.substring(10))

                val list = BuyListManager.getBuyList(`val`)
                if (list == null)
                    activeChar.sendMessage("Invalid buylist id.")
                else
                    activeChar.sendPacket(BuyList(list, activeChar.adena, 0.0))
            } catch (e: Exception) {
                activeChar.sendMessage("Invalid buylist id.")
            }

        } else if (command == "admin_gmshop")
            AdminHelpPage.showHelpPage(activeChar, "gmshops.htm")

        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf("admin_buy", "admin_gmshop")
    }
}