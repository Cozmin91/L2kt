package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.L2DatabaseFactory
import com.l2kt.gameserver.LoginServerThread
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * This class handles following admin commands:
 *
 *  * ban_acc [account_name] = changes account access level to -100 and logs him off. If no account is specified target's account is used.
 *  * ban_char [char_name] = changes a characters access level to -100 and logs him off. If no character is specified target is used.
 *  * ban_chat [char_name] [duration] = chat bans a character for the specified duration. If no name is specified the target is chat banned indefinitely.
 *  * unban_acc [account_name] = changes account access level to 0.
 *  * unban_char [char_name] = changes specified characters access level to 0.
 *  * unban_chat [char_name] = lifts chat ban from specified player. If no player name is specified current target is used.
 *  * jail [char_name] [penalty_time] = jails character. Time specified in minutes. For ever if no time is specified.
 *  * unjail [char_name] = Unjails player, teleport him to Floran.
 *
 */
class AdminBan : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        val st = StringTokenizer(command)
        st.nextToken()
        var player = ""
        var duration = -1
        var targetPlayer: Player? = null

        // One parameter, player name
        if (st.hasMoreTokens()) {
            player = st.nextToken()
            targetPlayer = World.getPlayer(player)

            // Second parameter, duration
            if (st.hasMoreTokens()) {
                try {
                    duration = Integer.parseInt(st.nextToken())
                } catch (nfe: NumberFormatException) {
                    activeChar.sendMessage("Invalid number format used: $nfe")
                    return false
                }

            }
        } else {
            // If there is no name, select target
            if (activeChar.target != null && activeChar.target is Player)
                targetPlayer = activeChar.target as Player
        }

        // Can't ban yourself
        if (targetPlayer != null && targetPlayer == activeChar) {
            activeChar.sendPacket(SystemMessageId.CANNOT_USE_ON_YOURSELF)
            return false
        }

        if (command.startsWith("admin_ban ") || command.equals("admin_ban", ignoreCase = true)) {
            activeChar.sendMessage("Available ban commands: //ban_acc, //ban_char, //ban_chat")
            return false
        } else if (command.startsWith("admin_ban_acc")) {
            if (targetPlayer == null && player == "") {
                activeChar.sendMessage("Usage: //ban_acc <account_name> (if none, target char's account gets banned).")
                return false
            }

            if (targetPlayer == null) {
                LoginServerThread.sendAccessLevel(player, -100)
                activeChar.sendMessage("Ban request sent for account $player.")
            } else {
                targetPlayer.setPunishLevel(Player.PunishLevel.ACC, 0)
                activeChar.sendMessage(targetPlayer.accountName + " account is now banned.")
            }
        } else if (command.startsWith("admin_ban_char")) {
            if (targetPlayer == null && player == "") {
                activeChar.sendMessage("Usage: //ban_char <char_name> (if none, target char is banned)")
                return false
            }

            return changeCharAccessLevel(targetPlayer, player, activeChar, -1)
        } else if (command.startsWith("admin_ban_chat")) {
            if (targetPlayer == null && player == "") {
                activeChar.sendMessage("Usage: //ban_chat <char_name> [penalty_minutes]")
                return false
            }

            if (targetPlayer != null) {
                if (targetPlayer.punishLevel.value() > 0) {
                    activeChar.sendMessage(targetPlayer.name + " is already jailed or banned.")
                    return false
                }

                var banLengthStr = ""
                targetPlayer.setPunishLevel(Player.PunishLevel.CHAT, duration)

                if (duration > 0)
                    banLengthStr = " for $duration minutes"

                activeChar.sendMessage(targetPlayer.name + " is now chat banned" + banLengthStr + ".")
            } else
                banChatOfflinePlayer(activeChar, player, duration, true)
        } else if (command.startsWith("admin_unban ") || command.equals("admin_unban", ignoreCase = true)) {
            activeChar.sendMessage("Available unban commands: //unban_acc, //unban_char, //unban_chat")
            return false
        } else if (command.startsWith("admin_unban_acc")) {
            if (targetPlayer != null) {
                activeChar.sendMessage(targetPlayer.name + " is currently online so mustn't be banned.")
                return false
            }

            if (player != "") {
                LoginServerThread.sendAccessLevel(player, 0)
                activeChar.sendMessage("Unban request sent for account $player.")
            } else {
                activeChar.sendMessage("Usage: //unban_acc <account_name>")
                return false
            }
        } else if (command.startsWith("admin_unban_char")) {
            if (targetPlayer == null && player == "") {
                activeChar.sendMessage("Usage: //unban_char <char_name>")
                return false
            }

            if (targetPlayer != null) {
                activeChar.sendMessage(targetPlayer.name + " is currently online so mustn't be banned.")
                return false
            }

            return changeCharAccessLevel(null, player, activeChar, 0)
        } else if (command.startsWith("admin_unban_chat")) {
            if (targetPlayer == null && player == "") {
                activeChar.sendMessage("Usage: //unban_chat <char_name>")
                return false
            }

            if (targetPlayer != null) {
                if (targetPlayer.isChatBanned) {
                    targetPlayer.setPunishLevel(Player.PunishLevel.NONE, 0)
                    activeChar.sendMessage(targetPlayer.name + "'s chat ban has been lifted.")
                } else
                    activeChar.sendMessage(targetPlayer.name + " isn't currently chat banned.")
            } else
                banChatOfflinePlayer(activeChar, player, 0, false)
        } else if (command.startsWith("admin_jail")) {
            if (targetPlayer == null && player == "") {
                activeChar.sendMessage("Usage: //jail <charname> [penalty_minutes] (if no name is given, selected target is jailed forever).")
                return false
            }

            if (targetPlayer != null) {
                targetPlayer.setPunishLevel(Player.PunishLevel.JAIL, duration)
                activeChar.sendMessage(targetPlayer.name + " has been jailed for " + if (duration > 0) duration.toString() + " minutes." else "ever !")
            } else
                jailOfflinePlayer(activeChar, player, duration)
        } else if (command.startsWith("admin_unjail")) {
            if (targetPlayer == null && player == "") {
                activeChar.sendMessage("Usage: //unjail <charname> (If no name is given target is used).")
                return false
            }

            if (targetPlayer != null) {
                targetPlayer.setPunishLevel(Player.PunishLevel.NONE, 0)
                activeChar.sendMessage(targetPlayer.name + " has been unjailed.")
            } else
                unjailOfflinePlayer(activeChar, player)
        }
        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val LOG = Logger.getLogger(AdminBan::class.java.name)

        private val UPDATE_BAN = "UPDATE characters SET punish_level=?, punish_timer=? WHERE char_name=?"
        private val UPDATE_JAIL =
            "UPDATE characters SET x=-114356, y=-249645, z=-2984, punish_level=?, punish_timer=? WHERE char_name=?"
        private val UPDATE_UNJAIL =
            "UPDATE characters SET x=17836, y=170178, z=-3507, punish_level=0, punish_timer=0 WHERE char_name=?"
        private val UPDATE_ACCESS = "UPDATE characters SET accesslevel=? WHERE char_name=?"

        private val ADMIN_COMMANDS = arrayOf(
            "admin_ban", // returns ban commands
            "admin_ban_acc", "admin_ban_char", "admin_ban_chat",

            "admin_unban", // returns unban commands
            "admin_unban_acc", "admin_unban_char", "admin_unban_chat",

            "admin_jail", "admin_unjail"
        )

        private fun banChatOfflinePlayer(activeChar: Player, name: String, delay: Int, ban: Boolean) {
            var level = 0
            var value: Long = 0

            if (ban) {
                level = Player.PunishLevel.CHAT.value()
                value = if (delay > 0) delay * 60000L else 60000
            } else
                level = Player.PunishLevel.NONE.value()

            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(UPDATE_BAN).use { ps ->
                        ps.setInt(1, level)
                        ps.setLong(2, value)
                        ps.setString(3, name)
                        ps.execute()

                        val count = ps.updateCount
                        if (count == 0)
                            activeChar.sendMessage("Character isn't found.")
                        else if (ban)
                            activeChar.sendMessage(name + " is chat banned for " + if (delay > 0) delay.toString() + " minutes." else "ever !")
                        else
                            activeChar.sendMessage("$name's chat ban has been lifted.")
                    }
                }
            } catch (e: Exception) {
                LOG.log(Level.SEVERE, "AdminBan.banChatOfflinePlayer :" + e.message, e)
            }

        }

        private fun jailOfflinePlayer(activeChar: Player, name: String, delay: Int) {
            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(UPDATE_JAIL).use { ps ->
                        ps.setInt(1, Player.PunishLevel.JAIL.value())
                        ps.setLong(2, if (delay > 0) delay * 60000L else 0)
                        ps.setString(3, name)
                        ps.execute()

                        val count = ps.updateCount
                        if (count == 0)
                            activeChar.sendMessage("Character not found!")
                        else
                            activeChar.sendMessage(name + " has been jailed for " + if (delay > 0) delay.toString() + " minutes." else "ever!")
                    }
                }
            } catch (e: Exception) {
                LOG.log(Level.SEVERE, "AdminBan.jailOfflinePlayer :" + e.message, e)
            }

        }

        private fun unjailOfflinePlayer(activeChar: Player, name: String) {
            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(UPDATE_UNJAIL).use { ps ->
                        ps.setString(1, name)
                        ps.execute()

                        val count = ps.updateCount
                        if (count == 0)
                            activeChar.sendMessage("Character isn't found.")
                        else
                            activeChar.sendMessage("$name has been unjailed.")
                    }
                }
            } catch (e: Exception) {
                LOG.log(Level.SEVERE, "AdminBan.unjailOfflinePlayer :" + e.message, e)
            }

        }

        private fun changeCharAccessLevel(
            targetPlayer: Player?,
            player: String,
            activeChar: Player,
            lvl: Int
        ): Boolean {
            if (targetPlayer != null) {
                targetPlayer.setAccessLevel(lvl)
                targetPlayer.logout(false)
                activeChar.sendMessage(targetPlayer.name + " has been banned.")
            } else {
                try {
                    L2DatabaseFactory.connection.use { con ->
                        con.prepareStatement(UPDATE_ACCESS).use { ps ->
                            ps.setInt(1, lvl)
                            ps.setString(2, player)
                            ps.execute()

                            val count = ps.updateCount
                            if (count == 0) {
                                activeChar.sendMessage("Character not found or access level unaltered.")
                                return false
                            }

                            activeChar.sendMessage("$player now has an access level of $lvl.")
                        }
                    }
                } catch (e: Exception) {
                    LOG.log(Level.SEVERE, "AdminBan.changeCharAccessLevel :" + e.message, e)
                    return false
                }

            }
            return true
        }
    }
}