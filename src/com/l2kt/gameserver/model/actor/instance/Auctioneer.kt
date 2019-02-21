package com.l2kt.gameserver.model.actor.instance

import com.l2kt.commons.lang.StringUtil
import com.l2kt.commons.math.MathUtil
import com.l2kt.gameserver.data.xml.MapRegionData
import com.l2kt.gameserver.instancemanager.AuctionManager
import com.l2kt.gameserver.instancemanager.ClanHallManager
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.entity.Auction
import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Auctioneer(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {

    private val _pendingAuctions = ConcurrentHashMap<Int, Auction>()

    override fun onBypassFeedback(player: Player, command: String) {
        val st = StringTokenizer(command, " ")

        val actualCommand = st.nextToken()
        val `val` = if (st.hasMoreTokens()) st.nextToken() else ""

        // Only a few actions are possible for clanless people.
        if (actualCommand.equals("list", ignoreCase = true)) {
            showAuctionsList(`val`, player)
            return
        } else if (actualCommand.equals("bidding", ignoreCase = true)) {
            if (`val`.isEmpty())
                return

            try {
                val auction = AuctionManager.getAuction(Integer.parseInt(`val`))
                if (auction != null) {
                    val ch = ClanHallManager.getClanHallById(auction.itemId)
                    val remainingTime = auction.endDate - System.currentTimeMillis()

                    val html = NpcHtmlMessage(objectId)
                    html.setFile("data/html/auction/AgitAuctionInfo.htm")
                    html.replace("%AGIT_NAME%", auction.itemName)
                    html.replace("%OWNER_PLEDGE_NAME%", auction.sellerClanName)
                    html.replace("%OWNER_PLEDGE_MASTER%", auction.sellerName)
                    html.replace("%AGIT_SIZE%", ch!!.grade * 10)
                    html.replace("%AGIT_LEASE%", ch.lease)
                    html.replace("%AGIT_LOCATION%", ch.location)
                    html.replace("%AGIT_AUCTION_END%", SimpleDateFormat("dd-MM-yyyy HH:mm").format(auction.endDate))
                    html.replace(
                        "%AGIT_AUCTION_REMAIN%",
                        (remainingTime / 3600000).toString() + " hours " + remainingTime / 60000 % 60 + " minutes"
                    )
                    html.replace("%AGIT_AUCTION_MINBID%", auction.startingBid)
                    html.replace("%AGIT_AUCTION_COUNT%", auction.bidders.size)
                    html.replace("%AGIT_AUCTION_DESC%", ch.desc)
                    html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + objectId + "_list")
                    html.replace("%AGIT_LINK_BIDLIST%", "bypass -h npc_" + objectId + "_bidlist " + auction.id)
                    html.replace("%AGIT_LINK_RE%", "bypass -h npc_" + objectId + "_bid1 " + auction.id)
                    player.sendPacket(html)
                }
            } catch (e: Exception) {
            }

            return
        } else if (actualCommand.equals("location", ignoreCase = true)) {
            val html = NpcHtmlMessage(objectId)
            html.setFile("data/html/auction/location.htm")
            html.replace("%location%", MapRegionData.getClosestTownName(player.x, player.y))
            html.replace("%LOCATION%", getPictureName(player))
            html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + objectId + "_start")
            player.sendPacket(html)
            return
        } else if (actualCommand.equals("start", ignoreCase = true)) {
            showChatWindow(player)
            return
        } else {
            if (player.clan == null || player.clanPrivileges and Clan.CP_CH_AUCTION != Clan.CP_CH_AUCTION) {
                showAuctionsList("1", player) // Force to display page 1.
                player.sendPacket(SystemMessageId.CANNOT_PARTICIPATE_IN_AUCTION)
                return
            }

            if (actualCommand.equals("bid", ignoreCase = true)) {
                if (`val`.isEmpty())
                    return

                try {
                    val bid =
                        if (st.hasMoreTokens()) Math.min(Integer.parseInt(st.nextToken()), Integer.MAX_VALUE) else 0

                    AuctionManager.getAuction(Integer.parseInt(`val`))!!.setBid(player, bid)
                } catch (e: Exception) {
                }

                return
            } else if (actualCommand.equals("bid1", ignoreCase = true)) {
                if (`val`.isEmpty())
                    return

                if (player.clan == null || player.clan.level < 2) {
                    showAuctionsList("1", player) // Force to display page 1.
                    player.sendPacket(SystemMessageId.AUCTION_ONLY_CLAN_LEVEL_2_HIGHER)
                    return
                }

                if (player.clan.hasHideout()) {
                    showAuctionsList("1", player) // Force to display page 1.
                    player.sendPacket(SystemMessageId.CANNOT_PARTICIPATE_IN_AUCTION)
                    return
                }

                try {
                    if (player.clan.auctionBiddedAt > 0 && player.clan.auctionBiddedAt != Integer.parseInt(`val`)) {
                        showAuctionsList("1", player) // Force to display page 1.
                        player.sendPacket(SystemMessageId.ALREADY_SUBMITTED_BID)
                        return
                    }

                    val auction = AuctionManager.getAuction(Integer.parseInt(`val`))
                    var minimumBid = auction!!.highestBidderMaxBid
                    if (minimumBid == 0)
                        minimumBid = auction.startingBid

                    val html = NpcHtmlMessage(objectId)
                    html.setFile("data/html/auction/AgitBid1.htm")
                    html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + objectId + "_bidding " + `val`)
                    html.replace("%PLEDGE_ADENA%", player.clan.warehouse.adena)
                    html.replace("%AGIT_AUCTION_MINBID%", minimumBid)
                    html.replace("npc_%objectId%_bid", "npc_" + objectId + "_bid " + `val`)
                    player.sendPacket(html)
                } catch (e: Exception) {
                }

                return
            } else if (actualCommand.equals("bidlist", ignoreCase = true)) {
                try {
                    var auctionId = 0
                    if (`val`.isEmpty()) {
                        if (player.clan.auctionBiddedAt <= 0)
                            return

                        auctionId = player.clan.auctionBiddedAt
                    } else
                        auctionId = Integer.parseInt(`val`)

                    val sdf = SimpleDateFormat("yyyy-MM-dd")
                    val bidders = AuctionManager.getAuction(auctionId)!!.bidders.values

                    val sb = StringBuilder(bidders.size * 150)
                    for (bidder in bidders)
                        StringUtil.append(
                            sb,
                            "<tr><td width=90 align=center>",
                            bidder.clanName,
                            "</td><td width=90 align=center>",
                            bidder.name,
                            "</td><td width=90 align=center>",
                            sdf.format(bidder.timeBid.time),
                            "</td></tr>"
                        )

                    val html = NpcHtmlMessage(objectId)
                    html.setFile("data/html/auction/AgitBidderList.htm")
                    html.replace("%AGIT_LIST%", sb.toString())
                    html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + objectId + "_bidding " + auctionId)
                    html.replace("%objectId%", objectId)
                    player.sendPacket(html)
                } catch (e: Exception) {
                }

                return
            } else if (actualCommand.equals("selectedItems", ignoreCase = true)) {
                showSelectedItems(player)
                return
            } else if (actualCommand.equals("cancelBid", ignoreCase = true)) {
                try {
                    val bid = AuctionManager.getAuction(player.clan.auctionBiddedAt)!!.bidders[player.clanId]!!.bid

                    val html = NpcHtmlMessage(objectId)
                    html.setFile("data/html/auction/AgitBidCancel.htm")
                    html.replace("%AGIT_BID%", bid)
                    html.replace("%AGIT_BID_REMAIN%", (bid * 0.9).toInt())
                    html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + objectId + "_selectedItems")
                    html.replace("%objectId%", objectId)
                    player.sendPacket(html)
                } catch (e: Exception) {
                }

                return
            } else if (actualCommand.equals("doCancelBid", ignoreCase = true)) {
                val auction = AuctionManager.getAuction(player.clan.auctionBiddedAt)
                if (auction != null) {
                    auction.cancelBid(player.clanId)
                    player.sendPacket(SystemMessageId.CANCELED_BID)
                }
                return
            } else if (actualCommand.equals("cancelAuction", ignoreCase = true)) {
                val html = NpcHtmlMessage(objectId)
                html.setFile("data/html/auction/AgitSaleCancel.htm")
                html.replace("%AGIT_DEPOSIT%", ClanHallManager.getClanHallByOwner(player.clan)!!.lease)
                html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + objectId + "_selectedItems")
                html.replace("%objectId%", objectId)
                player.sendPacket(html)
                return
            } else if (actualCommand.equals("doCancelAuction", ignoreCase = true)) {
                val auction = AuctionManager.getAuction(player.clan.hideoutId)
                if (auction != null) {
                    auction.cancelAuction()
                    player.sendPacket(SystemMessageId.CANCELED_BID)
                }
                showChatWindow(player)
                return
            } else if (actualCommand.equals("sale", ignoreCase = true)) {
                val html = NpcHtmlMessage(objectId)
                html.setFile("data/html/auction/AgitSale1.htm")
                html.replace("%AGIT_DEPOSIT%", ClanHallManager.getClanHallByOwner(player.clan)!!.lease)
                html.replace("%AGIT_PLEDGE_ADENA%", player.clan.warehouse.adena)
                html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + objectId + "_selectedItems")
                html.replace("%objectId%", objectId)
                player.sendPacket(html)
                return
            } else if (actualCommand.equals("rebid", ignoreCase = true)) {
                val auction = AuctionManager.getAuction(player.clan.auctionBiddedAt)
                if (auction != null) {
                    val html = NpcHtmlMessage(objectId)
                    html.setFile("data/html/auction/AgitBid2.htm")
                    html.replace("%AGIT_AUCTION_BID%", auction.bidders[player.clanId]!!.bid)
                    html.replace("%AGIT_AUCTION_MINBID%", auction.startingBid)
                    html.replace("%AGIT_AUCTION_END%", SimpleDateFormat("dd-MM-yyyy HH:mm").format(auction.endDate))
                    html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + objectId + "_selectedItems")
                    html.replace("npc_%objectId%_bid1", "npc_" + objectId + "_bid1 " + auction.id)
                    player.sendPacket(html)
                }
                return
            } else {
                if (player.clan.warehouse.adena < ClanHallManager.getClanHallByOwner(player.clan)!!.lease) {
                    showSelectedItems(player)
                    player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA_IN_CWH)
                    return
                }

                if (actualCommand.equals("auction", ignoreCase = true)) {
                    if (`val`.isEmpty())
                        return

                    try {
                        val days = Integer.parseInt(`val`)
                        val bid =
                            if (st.hasMoreTokens()) Math.min(Integer.parseInt(st.nextToken()), Integer.MAX_VALUE) else 0
                        val ch = ClanHallManager.getClanHallByOwner(player.clan)

                        val auction = Auction(player.clan.hideoutId, player.clan, days * 86400000L, bid, ch!!.name)

                        _pendingAuctions[auction.id] = auction

                        val html = NpcHtmlMessage(objectId)
                        html.setFile("data/html/auction/AgitSale3.htm")
                        html.replace("%x%", `val`)
                        html.replace("%AGIT_AUCTION_END%", SimpleDateFormat("dd-MM-yyyy HH:mm").format(auction.endDate))
                        html.replace("%AGIT_AUCTION_MINBID%", auction.startingBid)
                        html.replace("%AGIT_AUCTION_MIN%", auction.startingBid)
                        html.replace("%AGIT_AUCTION_DESC%", ch.desc)
                        html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + objectId + "_sale2")
                        html.replace("%objectId%", objectId)
                        player.sendPacket(html)
                    } catch (e: Exception) {
                    }

                    return
                } else if (actualCommand.equals("confirmAuction", ignoreCase = true)) {
                    val chId = player.clan.hideoutId
                    if (chId <= 0)
                        return

                    val auction = _pendingAuctions[chId] ?: return

                    if (Auction.takeItem(player, ClanHallManager.getClanHallByOwner(player.clan)!!.lease)) {
                        auction.confirmAuction()

                        _pendingAuctions.remove(chId)

                        showSelectedItems(player)
                        player.sendPacket(SystemMessageId.REGISTERED_FOR_CLANHALL)
                    }
                    return
                } else if (actualCommand.equals("sale2", ignoreCase = true)) {
                    val html = NpcHtmlMessage(objectId)
                    html.setFile("data/html/auction/AgitSale2.htm")
                    html.replace("%AGIT_LAST_PRICE%", ClanHallManager.getClanHallByOwner(player.clan)!!.lease)
                    html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + objectId + "_sale")
                    html.replace("%objectId%", objectId)
                    player.sendPacket(html)
                    return
                }
            }/* Those bypasses check if CWH got enough adenas (case of sale auction type) */
        }// Clanless or clan members without enough power are kicked directly.
        super.onBypassFeedback(player, command)
    }

    override fun showChatWindow(player: Player) {
        val html = NpcHtmlMessage(objectId)
        html.setFile("data/html/auction/auction.htm")
        html.replace("%objectId%", objectId)
        html.replace("%npcId%", npcId)
        html.replace("%npcname%", name)
        player.sendPacket(html)
    }

    override fun showChatWindow(player: Player, `val`: Int) {
        if (`val` == 0)
            return

        super.showChatWindow(player, `val`)
    }

    private fun showAuctionsList(`val`: String, player: Player) {
        // Retrieve the whole auctions list.
        var auctions = AuctionManager.auctions

        val page = if (`val`.isEmpty()) 1 else Integer.parseInt(`val`)
        val max = MathUtil.countPagesNumber(auctions.size, PAGE_LIMIT)

        // Cut auctions list up to page number.
        auctions = auctions.subList((page - 1) * PAGE_LIMIT, Math.min(page * PAGE_LIMIT, auctions.size))

        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val sb = StringBuilder(4000)

        sb.append("<table width=280>")

        // Auctions feeding.
        for (auction in auctions)
            StringUtil.append(
                sb,
                "<tr><td><font color=\"aaaaff\">",
                ClanHallManager.getClanHallById(auction.itemId)!!.location,
                "</font></td><td><font color=\"ffffaa\"><a action=\"bypass -h npc_",
                objectId,
                "_bidding ",
                auction.id,
                "\">",
                auction.itemName,
                " [",
                auction.bidders.size,
                "]</a></font></td><td>",
                sdf.format(auction.endDate),
                "</td><td><font color=\"aaffff\">",
                auction.startingBid,
                "</font></td></tr>"
            )

        sb.append("</table><table width=280><tr>")

        // Page feeding.
        for (j in 1..max)
            StringUtil.append(
                sb,
                "<td><center><a action=\"bypass -h npc_",
                objectId,
                "_list ",
                j,
                "\"> Page ",
                j,
                " </a></center></td>"
            )

        sb.append("</tr></table>")

        val html = NpcHtmlMessage(objectId)
        html.setFile("data/html/auction/AgitAuctionList.htm")
        html.replace("%AGIT_LIST%", sb.toString())
        html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + objectId + "_start")
        player.sendPacket(html)
        return
    }

    private fun showSelectedItems(player: Player) {
        val clan = player.clan ?: return

        if (!clan.hasHideout() && clan.auctionBiddedAt > 0) {
            val auction = AuctionManager.getAuction(clan.auctionBiddedAt)
            if (auction != null) {
                val ch = ClanHallManager.getClanHallById(auction.itemId)
                val remainingTime = auction.endDate - System.currentTimeMillis()

                val html = NpcHtmlMessage(objectId)
                html.setFile("data/html/auction/AgitBidInfo.htm")
                html.replace("%AGIT_NAME%", auction.itemName)
                html.replace("%OWNER_PLEDGE_NAME%", auction.sellerClanName)
                html.replace("%OWNER_PLEDGE_MASTER%", auction.sellerName)
                html.replace("%AGIT_SIZE%", ch!!.grade * 10)
                html.replace("%AGIT_LEASE%", ch.lease)
                html.replace("%AGIT_LOCATION%", ch.location)
                html.replace("%AGIT_AUCTION_END%", SimpleDateFormat("dd-MM-yyyy HH:mm").format(auction.endDate))
                html.replace(
                    "%AGIT_AUCTION_REMAIN%",
                    (remainingTime / 3600000).toString() + " hours " + remainingTime / 60000 % 60 + " minutes"
                )
                html.replace("%AGIT_AUCTION_MINBID%", auction.startingBid)
                html.replace("%AGIT_AUCTION_MYBID%", auction.bidders[player.clanId]!!.bid)
                html.replace("%AGIT_AUCTION_DESC%", ch.desc)
                html.replace("%objectId%", objectId)
                html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + objectId + "_start")
                player.sendPacket(html)
            }
            return
        } else if (AuctionManager.getAuction(clan.hideoutId) != null) {
            val auction = AuctionManager.getAuction(clan.hideoutId)
            if (auction != null) {
                val ch = ClanHallManager.getClanHallById(auction.itemId)
                val remainingTime = auction.endDate - System.currentTimeMillis()

                val html = NpcHtmlMessage(objectId)
                html.setFile("data/html/auction/AgitSaleInfo.htm")
                html.replace("%AGIT_NAME%", auction.itemName)
                html.replace("%AGIT_OWNER_PLEDGE_NAME%", auction.sellerClanName)
                html.replace("%OWNER_PLEDGE_MASTER%", auction.sellerName)
                html.replace("%AGIT_SIZE%", ch!!.grade * 10)
                html.replace("%AGIT_LEASE%", ch.lease)
                html.replace("%AGIT_LOCATION%", ch.location)
                html.replace("%AGIT_AUCTION_END%", SimpleDateFormat("dd-MM-yyyy HH:mm").format(auction.endDate))
                html.replace(
                    "%AGIT_AUCTION_REMAIN%",
                    (remainingTime / 3600000).toString() + " hours " + remainingTime / 60000 % 60 + " minutes"
                )
                html.replace("%AGIT_AUCTION_MINBID%", auction.startingBid)
                html.replace("%AGIT_AUCTION_BIDCOUNT%", auction.bidders.size)
                html.replace("%AGIT_AUCTION_DESC%", ch.desc)
                html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + objectId + "_start")
                html.replace("%id%", auction.id)
                html.replace("%objectId%", objectId)
                player.sendPacket(html)
            }
            return
        } else if (clan.hasHideout()) {
            val ch = ClanHallManager.getClanHallById(clan.hideoutId)

            val html = NpcHtmlMessage(objectId)
            html.setFile("data/html/auction/AgitInfo.htm")
            html.replace("%AGIT_NAME%", ch!!.name)
            html.replace("%AGIT_OWNER_PLEDGE_NAME%", clan.name)
            html.replace("%OWNER_PLEDGE_MASTER%", clan.leaderName)
            html.replace("%AGIT_SIZE%", ch.grade * 10)
            html.replace("%AGIT_LEASE%", ch.lease)
            html.replace("%AGIT_LOCATION%", ch.location)
            html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + objectId + "_start")
            html.replace("%objectId%", objectId)
            player.sendPacket(html)
            return
        } else if (!clan.hasHideout()) {
            showAuctionsList("1", player) // Force to display page 1.
            player.sendPacket(SystemMessageId.NO_OFFERINGS_OWN_OR_MADE_BID_FOR)
            return
        }
    }

    companion object {
        private val PAGE_LIMIT = 15

        private fun getPictureName(plyr: Player): String {
            when (MapRegionData.getMapRegion(plyr.x, plyr.y)) {
                5 -> return "GLUDIO"

                6 -> return "GLUDIN"

                7 -> return "DION"

                8 -> return "GIRAN"

                14 -> return "RUNE"

                15 -> return "GODARD"

                16 -> return "SCHUTTGART"

                else -> return "ADEN"
            }
        }
    }
}