package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.GMViewPledgeInfo
import java.util.*

/**
 * This handler handles pledge commands.<br></br>
 * <br></br>
 * With any player target:
 *
 *  * //pledge create **String**
 *
 * With clan member target:
 *
 *  * //pledge info
 *  * //pledge dismiss
 *  * //pledge setlevel **int**
 *  * //pledge rep **int**
 *
 */
class AdminPledge : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        val target = activeChar.target
        if (target !is Player) {
            activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
            showMainPage(activeChar)
            return false
        }

        if (command.startsWith("admin_pledge")) {
            val st = StringTokenizer(command, " ")
            try {
                st.nextToken()
                val action = st.nextToken()

                if (action == "create") {
                    try {
                        val parameter = st.nextToken()

                        val cet = target.clanCreateExpiryTime
                        target.clanCreateExpiryTime = 0
                        val clan = ClanTable.createClan(target, parameter)
                        if (clan != null)
                            activeChar.sendMessage("Clan " + parameter + " have been created. Clan leader is " + target.name + ".")
                        else {
                            target.clanCreateExpiryTime = cet
                            activeChar.sendMessage("There was a problem while creating the clan.")
                        }
                    } catch (e: Exception) {
                        activeChar.sendMessage("Invalid string parameter for //pledge create.")
                    }

                } else {
                    if (target.clan == null) {
                        activeChar.sendPacket(SystemMessageId.TARGET_MUST_BE_IN_CLAN)
                        showMainPage(activeChar)
                        return false
                    }

                    if (action == "dismiss") {
                        ClanTable.destroyClan(target.clan!!)

                        if (target.clan == null)
                            activeChar.sendMessage("The clan is now disbanded.")
                        else
                            activeChar.sendMessage("There was a problem while destroying the clan.")
                    } else if (action == "info")
                        activeChar.sendPacket(GMViewPledgeInfo(target.clan!!, target))
                    else if (action == "setlevel") {
                        try {
                            val level = Integer.parseInt(st.nextToken())

                            if (level >= 0 && level < 9) {
                                target.clan!!.changeLevel(level)
                                activeChar.sendMessage("You have set clan " + target.clan!!.name + " to level " + level)
                            } else
                                activeChar.sendMessage("This clan level is incorrect. Put a number between 0 and 8.")
                        } catch (e: Exception) {
                            activeChar.sendMessage("Invalid number parameter for //pledge setlevel.")
                        }

                    } else if (action.startsWith("rep")) {
                        try {
                            val points = Integer.parseInt(st.nextToken())
                            val clan = target.clan ?: return false

                            if (clan.level < 5) {
                                activeChar.sendMessage("Only clans of level 5 or above may receive reputation points.")
                                showMainPage(activeChar)
                                return false
                            }

                            clan.addReputationScore(points)
                            activeChar.sendMessage("You " + (if (points > 0) "added " else "removed ") + Math.abs(points) + " points " + (if (points > 0) "to " else "from ") + clan.name + "'s reputation. Their current score is: " + clan.reputationScore)
                        } catch (e: Exception) {
                            activeChar.sendMessage("Invalid number parameter for //pledge rep.")
                        }

                    }
                }
            } catch (e: Exception) {
                activeChar.sendMessage("Invalid action or parameter.")
            }

        }
        showMainPage(activeChar)
        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf("admin_pledge")

        private fun showMainPage(activeChar: Player) {
            AdminHelpPage.showHelpPage(activeChar, "game_menu.htm")
        }
    }
}