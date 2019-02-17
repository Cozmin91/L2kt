package com.l2kt.gameserver.communitybbs.Manager

import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.data.cache.HtmCache
import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.instancemanager.ClanHallManager
import com.l2kt.gameserver.model.actor.instance.Player
import java.text.SimpleDateFormat
import java.util.*

object RegionBBSManager : BaseBBSManager() {

    override fun parseCmd(command: String, player: Player) {
        when {
            command == "_bbsloc" -> showRegionsList(player)
            command.startsWith("_bbsloc") -> {
                val st = StringTokenizer(command, ";")
                st.nextToken()

                showRegion(player, Integer.parseInt(st.nextToken()))
            }
            else -> super.parseCmd(command, player)
        }
    }

    override val folder: String get() = "region/"

    private fun showRegionsList(player: Player) {
        val content = HtmCache.getHtm(BaseBBSManager.CB_PATH + "region/castlelist.htm")

        val sb = StringBuilder(500)
        for (castle in CastleManager.castles) {
            val owner = ClanTable.getClan(castle.ownerId)

            StringUtil.append(
                sb,
                "<table><tr><td width=5></td><td width=160><a action=\"bypass _bbsloc;",
                castle.castleId,
                "\">",
                castle.name,
                "</a></td><td width=160>",
                if (owner != null) "<a action=\"bypass _bbsclan;home;" + owner.clanId + "\">" + owner.name + "</a>" else "None",
                "</td><td width=160>",
                if (owner != null && owner.allyId > 0) owner.allyName ?: "None" else "None",
                "</td><td width=120>",
                if (owner != null) castle.taxPercent else "0",
                "</td><td width=5></td></tr></table><br1><img src=\"L2UI.Squaregray\" width=605 height=1><br1>"
            )
        }
        BaseBBSManager.separateAndSend(content.replace("%castleList%", sb.toString()), player)
    }

    private fun showRegion(player: Player, castleId: Int) {
        val castle = CastleManager.getCastleById(castleId) ?: return
        val owner = ClanTable.getClan(castle.ownerId)

        var content = HtmCache.getHtm(BaseBBSManager.CB_PATH + "region/castle.htm")

        content = content.replace("%castleName%", castle.name)
        content = content.replace("%tax%", Integer.toString(castle.taxPercent))
        content = content.replace("%lord%", if (owner != null) owner.leaderName else "None")
        content = content.replace(
            "%clanName%",
            if (owner != null) "<a action=\"bypass _bbsclan;home;" + owner.clanId + "\">" + owner.name + "</a>" else "None"
        )
        content = content.replace("%allyName%", if (owner != null && owner.allyId > 0) owner.allyName ?: "None" else "None")
        content = content.replace(
            "%siegeDate%",
            SimpleDateFormat("yyyy-MM-dd HH:mm").format(castle.siegeDate.timeInMillis)
        )

        val sb = StringBuilder(200)

        val clanHalls = ClanHallManager.getClanHallsByLocation(castle.name)
        if (clanHalls != null && !clanHalls.isEmpty()) {
            sb.append("<br><br><table width=610 bgcolor=A7A19A><tr><td width=5></td><td width=200>Clan Hall Name</td><td width=200>Owning Clan</td><td width=200>Clan Leader Name</td><td width=5></td></tr></table><br1>")

            for (ch in clanHalls) {
                val chOwner = ClanTable.getClan(ch.ownerId)

                StringUtil.append(
                    sb,
                    "<table><tr><td width=5></td><td width=200>",
                    ch.name,
                    "</td><td width=200>",
                    if (chOwner != null) "<a action=\"bypass _bbsclan;home;" + chOwner.clanId + "\">" + chOwner.name + "</a>" else "None",
                    "</td><td width=200>",
                    if (chOwner != null) chOwner.leaderName else "None",
                    "</td><td width=5></td></tr></table><br1><img src=\"L2UI.Squaregray\" width=605 height=1><br1>"
                )
            }
        }
        BaseBBSManager.separateAndSend(content.replace("%hallsList%", sb.toString()), player)
    }
}