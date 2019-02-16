package com.l2kt.gameserver.handler.usercommandhandlers

import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.handler.IUserCommandHandler
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.entity.Siege
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage

class SiegeStatus : IUserCommandHandler {

    override fun useUserCommand(id: Int, activeChar: Player): Boolean {
        if (!activeChar.isClanLeader) {
            activeChar.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CAN_ISSUE_COMMANDS)
            return false
        }

        if (!activeChar.isNoble) {
            activeChar.sendPacket(SystemMessageId.ONLY_NOBLESSE_LEADER_CAN_VIEW_SIEGE_STATUS_WINDOW)
            return false
        }

        val clan = activeChar.clan

        val sb = StringBuilder()

        for (castle in CastleManager.castles) {
            // Search on lists : as a clan can only be registered in a single siege, break after one case is found.
            if (!castle.siege.isInProgress || !castle.siege.checkSides(
                    clan,
                    Siege.SiegeSide.ATTACKER,
                    Siege.SiegeSide.DEFENDER,
                    Siege.SiegeSide.OWNER
                )
            )
                continue

            for (member in clan.onlineMembers)
                StringUtil.append(
                    sb,
                    "<tr><td width=170>",
                    member.name,
                    "</td><td width=100>",
                    if (castle.siegeZone.isInsideZone(member)) IN_PROGRESS else OUTSIDE_ZONE,
                    "</td></tr>"
                )

            val html = NpcHtmlMessage(0)
            html.setFile("data/html/siege_status.htm")
            html.replace("%kills%", clan.siegeKills)
            html.replace("%deaths%", clan.siegeDeaths)
            html.replace("%content%", sb.toString())
            activeChar.sendPacket(html)
            return true
        }

        activeChar.sendPacket(SystemMessageId.ONLY_DURING_SIEGE)
        return false
    }

    override val userCommandList: IntArray get() = COMMAND_IDS

    companion object {
        private val COMMAND_IDS = intArrayOf(99)

        private val IN_PROGRESS = "Castle Siege in Progress"
        private val OUTSIDE_ZONE = "Outside Castle Siege Zone"
    }
}