package com.l2kt.gameserver.model.entity

import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.instancemanager.AuctionManager
import com.l2kt.gameserver.instancemanager.ClanHallManager
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.sql.PreparedStatement
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

class Auction {
    /**
     * @return the auction id.
     */
    var id = 0
        private set
    var endDate: Long = 0
        private set
    var highestBidderId = 0
        private set
    var highestBidderName = ""
        private set
    var highestBidderMaxBid = 0
        private set
    var itemId = 0
        private set
    var itemName = ""
        private set
    var sellerId = 0
        private set
    var sellerClanName = ""
        private set
    var sellerName = ""
        private set
    var currentBid = 0
        private set
    var startingBid = 0
        private set

    private val _bidders = HashMap<Int, Bidder>()

    val bidders: Map<Int, Bidder>
        get() = _bidders

    inner class Bidder(val name: String, val clanName: String, var bid: Int, timeBid: Long) {
        val timeBid: Calendar

        init {
            this.timeBid = Calendar.getInstance()
            this.timeBid.timeInMillis = timeBid
        }

        fun setTimeBid(timeBid: Long) {
            this.timeBid.timeInMillis = timeBid
        }
    }

    /** Task Sheduler for endAuction  */
    inner class AutoEndTask : Runnable {

        override fun run() {
            try {
                endAuction()
            } catch (e: Exception) {
                _log.log(Level.SEVERE, "", e)
            }

        }
    }

    /**
     * Constructor
     * @param auctionId The id linked to that auction
     */
    constructor(auctionId: Int) {
        id = auctionId
        load()
        startAutoTask()
    }

    constructor(itemId: Int, Clan: Clan, delay: Long, bid: Int, name: String) {
        id = itemId
        endDate = System.currentTimeMillis() + delay
        this.itemId = itemId
        itemName = name
        sellerId = Clan.leaderId
        sellerName = Clan.leaderName
        sellerClanName = Clan.name
        startingBid = bid
    }

    /** Load auctions  */
    private fun load() {
        try {
            L2DatabaseFactory.connection.use { con ->
                val statement = con.prepareStatement("SELECT * FROM auction WHERE id = ?")
                statement.setInt(1, id)
                val rs = statement.executeQuery()

                while (rs.next()) {
                    currentBid = rs.getInt("currentBid")
                    endDate = rs.getLong("endDate")
                    itemId = rs.getInt("itemId")
                    itemName = rs.getString("itemName")
                    sellerId = rs.getInt("sellerId")
                    sellerClanName = rs.getString("sellerClanName")
                    sellerName = rs.getString("sellerName")
                    startingBid = rs.getInt("startingBid")
                }
                rs.close()
                statement.close()
                loadBid()
            }
        } catch (e: Exception) {
            _log.log(Level.WARNING, "Exception: Auction.load(): " + e.message, e)
        }

    }

    /** Load bidders  */
    private fun loadBid() {
        highestBidderId = 0
        highestBidderName = ""
        highestBidderMaxBid = 0

        try {
            L2DatabaseFactory.connection.use { con ->
                val statement =
                    con.prepareStatement("SELECT bidderId, bidderName, maxBid, clan_name, time_bid FROM auction_bid WHERE auctionId = ? ORDER BY maxBid DESC")
                statement.setInt(1, id)
                val rs = statement.executeQuery()

                while (rs.next()) {
                    if (rs.isFirst) {
                        highestBidderId = rs.getInt("bidderId")
                        highestBidderName = rs.getString("bidderName")
                        highestBidderMaxBid = rs.getInt("maxBid")
                    }
                    _bidders[rs.getInt("bidderId")] = Bidder(
                        rs.getString("bidderName"),
                        rs.getString("clan_name"),
                        rs.getInt("maxBid"),
                        rs.getLong("time_bid")
                    )
                }
                rs.close()
                statement.close()
            }
        } catch (e: Exception) {
            _log.log(Level.WARNING, "Exception: Auction.loadBid(): " + e.message, e)
        }

    }

    /** Task Manage  */
    private fun startAutoTask() {
        val currentTime = System.currentTimeMillis()
        var taskDelay: Long = 0

        if (endDate <= currentTime) {
            endDate = currentTime + 604800000 // 1 week
            saveAuctionDate()
        } else
            taskDelay = endDate - currentTime

        ThreadPool.schedule(AutoEndTask(), taskDelay)
    }

    /** Save Auction Data End  */
    private fun saveAuctionDate() {
        try {
            L2DatabaseFactory.connection.use { con ->
                val statement = con.prepareStatement("Update auction set endDate = ? where id = ?")
                statement.setLong(1, endDate)
                statement.setInt(2, id)
                statement.execute()
                statement.close()
            }
        } catch (e: Exception) {
            _log.log(Level.SEVERE, "Exception: saveAuctionDate(): " + e.message, e)
        }

    }

    /**
     * Set a bid
     * @param bidder The bidder.
     * @param bid The bid amount.
     */
    @Synchronized
    fun setBid(bidder: Player, bid: Int) {
        var requiredAdena = bid

        if (highestBidderName == bidder.clan.leaderName)
            requiredAdena = bid - highestBidderMaxBid

        if (highestBidderId > 0 && bid > highestBidderMaxBid || highestBidderId == 0 && bid >= startingBid) {
            if (takeItem(bidder, requiredAdena)) {
                updateInDB(bidder, bid)
                bidder.clan.auctionBiddedAt = id
                return
            }
        }

        if (bid < startingBid || bid <= highestBidderMaxBid)
            bidder.sendPacket(SystemMessageId.BID_PRICE_MUST_BE_HIGHER)
    }

    /**
     * Update auction in DB
     * @param bidder The bidder to make checks on.
     * @param bid The related bid id.
     */
    private fun updateInDB(bidder: Player, bid: Int) {
        try {
            L2DatabaseFactory.connection.use { con ->
                val statement: PreparedStatement

                if (bidders[bidder.clanId] != null) {
                    statement =
                            con.prepareStatement("UPDATE auction_bid SET bidderId=?, bidderName=?, maxBid=?, time_bid=? WHERE auctionId=? AND bidderId=?")
                    statement.setInt(1, bidder.clanId)
                    statement.setString(2, bidder.clan.leaderName)
                    statement.setInt(3, bid)
                    statement.setLong(4, System.currentTimeMillis())
                    statement.setInt(5, id)
                    statement.setInt(6, bidder.clanId)
                    statement.execute()
                    statement.close()
                } else {
                    statement =
                            con.prepareStatement("INSERT INTO auction_bid (id, auctionId, bidderId, bidderName, maxBid, clan_name, time_bid) VALUES (?, ?, ?, ?, ?, ?, ?)")
                    statement.setInt(1, IdFactory.getInstance().nextId)
                    statement.setInt(2, id)
                    statement.setInt(3, bidder.clanId)
                    statement.setString(4, bidder.name)
                    statement.setInt(5, bid)
                    statement.setString(6, bidder.clan.name)
                    statement.setLong(7, System.currentTimeMillis())
                    statement.execute()
                    statement.close()
                }

                highestBidderId = bidder.clanId
                highestBidderMaxBid = bid
                highestBidderName = bidder.clan.leaderName

                if (_bidders[highestBidderId] == null)
                    _bidders[highestBidderId] =
                            Bidder(highestBidderName, bidder.clan.name, bid, Calendar.getInstance().timeInMillis)
                else {
                    _bidders[highestBidderId]!!.bid = bid
                    _bidders[highestBidderId]!!.setTimeBid(Calendar.getInstance().timeInMillis)
                }
                bidder.sendPacket(SystemMessageId.BID_IN_CLANHALL_AUCTION)
            }
        } catch (e: Exception) {
            _log.log(Level.SEVERE, "Exception: Auction.updateInDB(Player bidder, int bid): " + e.message, e)
        }

    }

    /**
     * Remove bids.
     * @param newOwner The Clan object who won the bid.
     */
    private fun removeBids(newOwner: Clan?) {
        try {
            L2DatabaseFactory.connection.use { con ->
                val statement = con.prepareStatement("DELETE FROM auction_bid WHERE auctionId=?")
                statement.setInt(1, id)
                statement.execute()
                statement.close()
            }
        } catch (e: Exception) {
            _log.log(Level.SEVERE, "Exception: Auction.deleteFromDB(): " + e.message, e)
        }

        var biddingClan: Clan?
        for (b in _bidders.values) {
            biddingClan = ClanTable.getClanByName(b.clanName)
            biddingClan!!.auctionBiddedAt = 0

            if (biddingClan != newOwner)
                returnItem(b.clanName, b.bid, true) // 10 % tax

            biddingClan.broadcastToOnlineMembers(
                SystemMessage.getSystemMessage(SystemMessageId.CLANHALL_AWARDED_TO_CLAN_S1).addString(
                    newOwner!!.name
                )
            )
        }
        _bidders.clear()
    }

    /** Remove auctions  */
    fun deleteAuctionFromDB() {
        AuctionManager.auctions.remove(this)
        try {
            L2DatabaseFactory.connection.use { con ->
                val statement = con.prepareStatement("DELETE FROM auction WHERE itemId=?")
                statement.setInt(1, itemId)
                statement.execute()
                statement.close()
            }
        } catch (e: Exception) {
            _log.log(Level.SEVERE, "Exception: Auction.deleteFromDB(): " + e.message, e)
        }

    }

    /** End of auction  */
    fun endAuction() {
        if (ClanHallManager.loaded()) {
            if (highestBidderId == 0 && sellerId == 0) {
                startAutoTask()
                return
            }

            // If seller hasn't sell clanHall, the auction is dropped. Money of seller is lost.
            if (highestBidderId == 0 && sellerId > 0) {
                val aucId = AuctionManager.getAuctionIndex(id)
                AuctionManager.auctions.removeAt(aucId)

                // Retrieves the seller.
                val owner = ClanTable.getClanByName(sellerClanName)
                owner!!.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLANHALL_NOT_SOLD))
                return
            }

            // Return intial lease of seller + highest bid amount.
            if (sellerId > 0) {
                returnItem(sellerClanName, highestBidderMaxBid, true)
                returnItem(sellerClanName, ClanHallManager.getClanHallById(itemId)!!.lease, false)
            }

            deleteAuctionFromDB()

            val newOwner = ClanTable.getClanByName(_bidders[highestBidderId]?.clanName ?: "")
            removeBids(newOwner)
            ClanHallManager.setOwner(itemId, newOwner!!)
        } else
            ThreadPool.schedule(AutoEndTask(), 3000)// Task waiting ClanHallManager is loaded every 3s
    }

    /**
     * Cancel bid
     * @param bidder The bidder id.
     */
    @Synchronized
    fun cancelBid(bidder: Int) {
        try {
            L2DatabaseFactory.connection.use { con ->
                val statement = con.prepareStatement("DELETE FROM auction_bid WHERE auctionId=? AND bidderId=?")
                statement.setInt(1, id)
                statement.setInt(2, bidder)
                statement.execute()
                statement.close()
            }
        } catch (e: Exception) {
            _log.log(Level.SEVERE, "Exception: Auction.cancelBid(String bidder): " + e.message, e)
        }

        if(_bidders[bidder] != null){
            returnItem(_bidders[bidder]!!.clanName, _bidders[bidder]!!.bid, true)
            ClanTable.getClanByName(_bidders[bidder]!!.clanName)!!.auctionBiddedAt = 0
        }

        _bidders.clear()
        loadBid()
    }

    /** Cancel auction  */
    fun cancelAuction() {
        deleteAuctionFromDB()
        removeBids(ClanTable.getClanByName(sellerClanName))
    }

    /** Confirm an auction  */
    fun confirmAuction() {
        AuctionManager.auctions.add(this)
        try {
            L2DatabaseFactory.connection.use { con ->
                val statement =
                    con.prepareStatement("INSERT INTO auction (id, sellerId, sellerName, sellerClanName, itemId, itemName, startingBid, currentBid, endDate) VALUES (?,?,?,?,?,?,?,?,?)")
                statement.setInt(1, id)
                statement.setInt(2, sellerId)
                statement.setString(3, sellerName)
                statement.setString(4, sellerClanName)
                statement.setInt(5, itemId)
                statement.setString(6, itemName)
                statement.setInt(7, startingBid)
                statement.setInt(8, currentBid)
                statement.setLong(9, endDate)
                statement.execute()
                statement.close()
                loadBid()
            }
        } catch (e: Exception) {
            _log.log(Level.SEVERE, "Exception: Auction.load(): " + e.message, e)
        }

    }

    companion object {
        protected val _log = Logger.getLogger(Auction::class.java.name)

        /**
         * Return Item in WHC
         * @param Clan The clan to make warehouse checks on.
         * @param quantity amount of returned adenas.
         * @param penalty if true, 10% of quantity is lost.
         */
        private fun returnItem(Clan: String, quantity: Int, penalty: Boolean) {
            var currentQuantity: Double = quantity.toDouble()
            if (penalty)
                currentQuantity *= 0.9 // Take 10% tax fee if needed

            // avoid overflow on return
            val limit = Integer.MAX_VALUE - ClanTable.getClanByName(Clan)!!.warehouse.adena
            currentQuantity = Math.min(currentQuantity, limit.toDouble())

            ClanTable.getClanByName(Clan)!!.warehouse.addItem("Outbidded", 57, currentQuantity.toInt(), null, null)
        }

        /**
         * Take Item in WHC
         * @param bidder The bidder to make checks on.
         * @param quantity amount of money.
         * @return true if successful.
         */
        fun takeItem(bidder: Player, quantity: Int): Boolean {
            if (bidder.clan != null && bidder.clan.warehouse.adena >= quantity) {
                bidder.clan.warehouse.destroyItemByItemId("Buy", 57, quantity, bidder, bidder)
                return true
            }

            bidder.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA_IN_CWH)
            return false
        }
    }
}