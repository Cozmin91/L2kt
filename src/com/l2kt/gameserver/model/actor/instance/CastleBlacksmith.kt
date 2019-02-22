package com.l2kt.gameserver.model.actor.instance

import com.l2kt.Config
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage

class CastleBlacksmith(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {

    override fun onBypassFeedback(player: Player, command: String) {
        if (!Config.ALLOW_MANOR) {
            val html = NpcHtmlMessage(objectId)
            html.setFile("data/html/npcdefault.htm")
            html.replace("%objectId%", objectId)
            html.replace("%npcname%", name)
            player.sendPacket(html)
            return
        }

        if (validateCondition(player) != COND_OWNER)
            return

        if (command.startsWith("Chat")) {
            var `val` = 0
            try {
                `val` = Integer.parseInt(command.substring(5))
            } catch (ioobe: IndexOutOfBoundsException) {
            } catch (nfe: NumberFormatException) {
            }

            showChatWindow(player, `val`)
        } else
            super.onBypassFeedback(player, command)
    }

    override fun showChatWindow(player: Player, `val`: Int) {
        if (!Config.ALLOW_MANOR) {
            val html = NpcHtmlMessage(objectId)
            html.setFile("data/html/npcdefault.htm")
            html.replace("%objectId%", objectId)
            html.replace("%npcname%", name)
            player.sendPacket(html)
            return
        }

        var filename = "data/html/castleblacksmith/castleblacksmith-no.htm"

        val condition = validateCondition(player)
        if (condition > COND_ALL_FALSE) {
            if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
                filename = "data/html/castleblacksmith/castleblacksmith-busy.htm" // Busy because of siege
            else if (condition == COND_OWNER)
            // Clan owns castle
            {
                if (`val` == 0)
                    filename = "data/html/castleblacksmith/castleblacksmith.htm"
                else
                    filename = "data/html/castleblacksmith/castleblacksmith-$`val`.htm"
            }
        }

        val html = NpcHtmlMessage(objectId)
        html.setFile(filename)
        html.replace("%objectId%", objectId)
        html.replace("%npcname%", name)
        html.replace("%castleid%", castle!!.castleId)
        player.sendPacket(html)
    }

    protected fun validateCondition(player: Player): Int {
        if (castle != null && player.clan != null) {
            if (castle!!.siege.isInProgress)
                return COND_BUSY_BECAUSE_OF_SIEGE

            if (castle!!.ownerId == player.clanId && player.clanPrivileges and Clan.CP_CS_MANOR_ADMIN == Clan.CP_CS_MANOR_ADMIN)
                return COND_OWNER
        }
        return COND_ALL_FALSE
    }

    companion object {
        protected const val COND_ALL_FALSE = 0
        protected const val COND_BUSY_BECAUSE_OF_SIEGE = 1
        protected const val COND_OWNER = 2
    }
}