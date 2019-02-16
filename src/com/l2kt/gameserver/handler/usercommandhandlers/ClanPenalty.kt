package com.l2kt.gameserver.handler.usercommandhandlers

import java.text.SimpleDateFormat

import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.handler.IUserCommandHandler
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.entity.Castle
import com.l2kt.gameserver.model.pledge.Clan

import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage

class ClanPenalty : IUserCommandHandler {

    override fun useUserCommand(id: Int, activeChar: Player): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val sb = StringBuilder()
        val currentTime = System.currentTimeMillis()

        // Join a clan penalty.
        if (activeChar.clanJoinExpiryTime > currentTime)
            StringUtil.append(
                sb,
                "<tr><td width=170>Unable to join a clan.</td><td width=100 align=center>",
                sdf.format(activeChar.clanJoinExpiryTime),
                "</td></tr>"
            )

        // Create a clan penalty.
        if (activeChar.clanCreateExpiryTime > currentTime)
            StringUtil.append(
                sb,
                "<tr><td width=170>Unable to create a clan.</td><td width=100 align=center>",
                sdf.format(activeChar.clanCreateExpiryTime),
                "</td></tr>"
            )

        val clan = activeChar.clan
        if (clan != null) {
            // Invitation in a clan penalty.
            if (clan.charPenaltyExpiryTime > currentTime)
                StringUtil.append(
                    sb,
                    "<tr><td width=170>Unable to invite a clan member.</td><td width=100 align=center>",
                    sdf.format(clan.charPenaltyExpiryTime),
                    "</td></tr>"
                )

            // Alliance penalties.
            val penaltyType = clan.allyPenaltyType
            if (penaltyType != 0) {
                val expiryTime = clan.allyPenaltyExpiryTime
                if (expiryTime > currentTime) {
                    // Unable to join an alliance.
                    if (penaltyType == Clan.PENALTY_TYPE_CLAN_LEAVED || penaltyType == Clan.PENALTY_TYPE_CLAN_DISMISSED)
                        StringUtil.append(
                            sb,
                            "<tr><td width=170>Unable to join an alliance.</td><td width=100 align=center>",
                            sdf.format(expiryTime),
                            "</td></tr>"
                        )
                    else if (penaltyType == Clan.PENALTY_TYPE_DISMISS_CLAN)
                        StringUtil.append(
                            sb,
                            "<tr><td width=170>Unable to invite a new alliance member.</td><td width=100 align=center>",
                            sdf.format(expiryTime),
                            "</td></tr>"
                        )
                    else if (penaltyType == Clan.PENALTY_TYPE_DISSOLVE_ALLY)
                        StringUtil.append(
                            sb,
                            "<tr><td width=170>Unable to create an alliance.</td><td width=100 align=center>",
                            sdf.format(expiryTime),
                            "</td></tr>"
                        )// Unable to create an alliance.
                    // Unable to invite a new alliance member.
                }
            }

            // Clan dissolution request.
            if (clan.dissolvingExpiryTime > currentTime)
                StringUtil.append(
                    sb,
                    "<tr><td width=170>The request to dissolve the clan is currently being processed.  (Restrictions are now going to be imposed on the use of clan functions.)</td><td width=100 align=center>",
                    sdf.format(clan.dissolvingExpiryTime),
                    "</td></tr>"
                )

            var registeredOnAnySiege = false
            for (castle in CastleManager.castles) {
                if (castle.siege.checkSides(clan)) {
                    registeredOnAnySiege = true
                    break
                }
            }

            // Unable to dissolve a clan.
            if (clan.allyId != 0 || clan.isAtWar || clan.hasCastle() || clan.hasHideout() || registeredOnAnySiege)
                StringUtil.append(sb, "<tr><td width=170>Unable to dissolve a clan.</td><td></td></tr>")
        }

        val html = NpcHtmlMessage(0)
        html.setFile("data/html/clan_penalty.htm")
        html.replace("%content%", if (sb.length == 0) NO_PENALTY else sb.toString())
        activeChar.sendPacket(html)
        return true
    }

    override val userCommandList: IntArray get() = COMMAND_IDS

    companion object {
        private val NO_PENALTY = "<tr><td width=170>No penalty is imposed.</td><td width=100 align=center></td></tr>"

        private val COMMAND_IDS = intArrayOf(100)
    }
}