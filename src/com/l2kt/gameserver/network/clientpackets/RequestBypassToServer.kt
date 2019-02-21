package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.communitybbs.CommunityBoard
import com.l2kt.gameserver.data.xml.AdminData
import com.l2kt.gameserver.handler.AdminCommandHandler
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.OlympiadManagerNpc
import com.l2kt.gameserver.model.entity.Hero
import com.l2kt.gameserver.model.olympiad.OlympiadManager
import com.l2kt.gameserver.network.FloodProtectors
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import java.util.*
import java.util.logging.Logger

class RequestBypassToServer : L2GameClientPacket() {

    private var _command: String = ""

    override fun readImpl() {
        _command = readS()
    }

    override fun runImpl() {
        if (_command.isEmpty())
            return

        if (!FloodProtectors.performAction(client, FloodProtectors.Action.SERVER_BYPASS))
            return

        val player = client.activeChar ?: return

        if (_command.startsWith("admin_")) {
            val command = _command.split(" ").dropLastWhile { it.isEmpty() }.toTypedArray()[0]

            val ach = AdminCommandHandler.getHandler(command)
            if (ach == null) {
                if (player.isGM)
                    player.sendMessage("The command " + command.substring(6) + " doesn't exist.")

                L2GameClientPacket.LOGGER.warn("No handler registered for admin command '{}'.", command)
                return
            }

            if (!AdminData.hasAccess(command, player.accessLevel)) {
                player.sendMessage("You don't have the access rights to use this command.")
                L2GameClientPacket.LOGGER.warn(
                    "{} tried to use admin command '{}' without proper Access Level.",
                    player.name,
                    command
                )
                return
            }

            if (Config.GMAUDIT)
                GMAUDIT_LOG.info(player.name + " [" + player.objectId + "] used '" + _command + "' command on: " + if (player.target != null) player.target.name else "none")

            ach.useAdminCommand(_command, player)
        } else if (_command.startsWith("player_help ")) {
            val path = _command.substring(12)
            if (path.indexOf("..") != -1)
                return

            val st = StringTokenizer(path)
            val cmd = st.nextToken().split("#").dropLastWhile { it.isEmpty() }.toTypedArray()

            val html = NpcHtmlMessage(0)
            html.setFile("data/html/help/" + cmd[0])
            if (cmd.size > 1)
                html.setItemId(Integer.parseInt(cmd[1]))
            html.disableValidation()
            player.sendPacket(html)
        } else if (_command.startsWith("npc_")) {
            if (!player.validateBypass(_command))
                return

            val endOfId = _command.indexOf('_', 5)
            val id: String
            id = if (endOfId > 0)
                _command.substring(4, endOfId)
            else
                _command.substring(4)

            try {
                val `object` = World.getObject(Integer.parseInt(id))

                if (`object` != null && `object` is Npc && endOfId > 0 && `object`.canInteract(player))
                    `object`.onBypassFeedback(player, _command.substring(endOfId + 1))

                player.sendPacket(ActionFailed.STATIC_PACKET)
            } catch (nfe: NumberFormatException) {
            }

        } else if (_command.startsWith("manor_menu_select?")) {
            val `object` = player.target
            if (`object` is Npc)
                `object`.onBypassFeedback(player, _command)
        } else if (_command.startsWith("bbs_") || _command.startsWith("_bbs") || _command.startsWith("_friend") || _command.startsWith(
                "_mail"
            ) || _command.startsWith("_block")
        ) {
            CommunityBoard.handleCommands(client, _command)
        } else if (_command.startsWith("Quest ")) {
            if (!player.validateBypass(_command))
                return

            val str = _command.substring(6).trim { it <= ' ' }.split(" ".toRegex(), 2).toTypedArray()
            if (str.size == 1)
                player.processQuestEvent(str[0], "")
            else
                player.processQuestEvent(str[0], str[1])
        } else if (_command.startsWith("_match")) {
            val params = _command.substring(_command.indexOf("?") + 1)
            val st = StringTokenizer(params, "&")
            val heroclass =
                Integer.parseInt(st.nextToken().split("=").dropLastWhile { it.isEmpty() }.toTypedArray()[1])
            val heropage =
                Integer.parseInt(st.nextToken().split("=").dropLastWhile { it.isEmpty() }.toTypedArray()[1])
            val heroid = Hero.getHeroByClass(heroclass)
            if (heroid > 0)
                Hero.showHeroFights(player, heroclass, heroid, heropage)
        } else if (_command.startsWith("_diary")) {
            val params = _command.substring(_command.indexOf("?") + 1)
            val st = StringTokenizer(params, "&")
            val heroclass =
                Integer.parseInt(st.nextToken().split("=").dropLastWhile { it.isEmpty() }.toTypedArray()[1])
            val heropage =
                Integer.parseInt(st.nextToken().split("=").dropLastWhile { it.isEmpty() }.toTypedArray()[1])
            val heroid = Hero.getHeroByClass(heroclass)
            if (heroid > 0)
                Hero.showHeroDiary(player, heroclass, heroid, heropage)
        } else if (_command.startsWith("arenachange"))
        // change
        {
            val isManager = player.currentFolk is OlympiadManagerNpc
            if (!isManager) {
                // Without npc, command can be used only in observer mode on arena
                if (!player.isInObserverMode || player.isInOlympiadMode || player.olympiadGameId < 0)
                    return
            }

            if (OlympiadManager.isRegisteredInComp(player)) {
                player.sendPacket(SystemMessageId.WHILE_YOU_ARE_ON_THE_WAITING_LIST_YOU_ARE_NOT_ALLOWED_TO_WATCH_THE_GAME)
                return
            }

            val arenaId = Integer.parseInt(_command.substring(12).trim { it <= ' ' })
            player.enterOlympiadObserverMode(arenaId)
        }// Navigate throught Manor windows
    }

    companion object {
        private val GMAUDIT_LOG = Logger.getLogger("gmaudit")
    }
}