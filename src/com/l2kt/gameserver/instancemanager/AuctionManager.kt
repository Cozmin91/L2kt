package com.l2kt.gameserver.instancemanager

import com.l2kt.L2DatabaseFactory
import com.l2kt.gameserver.model.entity.Auction
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

object AuctionManager {
    private val _auctions: MutableList<Auction>
    private val _log = Logger.getLogger(AuctionManager::class.java.name)

    private val ITEM_INIT_DATA = arrayOf(
        "(22, 0, '', '', 22, 'Moonstone Hall', 20000000, 0, 1164841200000)",
        "(23, 0, '', '', 23, 'Onyx Hall', 20000000, 0, 1164841200000)",
        "(24, 0, '', '', 24, 'Topaz Hall', 20000000, 0, 1164841200000)",
        "(25, 0, '', '', 25, 'Ruby Hall', 20000000, 0, 1164841200000)",
        "(26, 0, '', '', 26, 'Crystal Hall', 20000000, 0, 1164841200000)",
        "(27, 0, '', '', 27, 'Onyx Hall', 20000000, 0, 1164841200000)",
        "(28, 0, '', '', 28, 'Sapphire Hall', 20000000, 0, 1164841200000)",
        "(29, 0, '', '', 29, 'Moonstone Hall', 20000000, 0, 1164841200000)",
        "(30, 0, '', '', 30, 'Emerald Hall', 20000000, 0, 1164841200000)",
        "(31, 0, '', '', 31, 'Atramental Barracks', 8000000, 0, 1164841200000)",
        "(32, 0, '', '', 32, 'Scarlet Barracks', 8000000, 0, 1164841200000)",
        "(33, 0, '', '', 33, 'Viridian Barracks', 8000000, 0, 1164841200000)",
        "(36, 0, '', '', 36, 'Golden Chamber', 50000000, 0, 1164841200000)",
        "(37, 0, '', '', 37, 'Silver Chamber', 50000000, 0, 1164841200000)",
        "(38, 0, '', '', 38, 'Mithril Chamber', 50000000, 0, 1164841200000)",
        "(39, 0, '', '', 39, 'Silver Manor', 50000000, 0, 1164841200000)",
        "(40, 0, '', '', 40, 'Gold Manor', 50000000, 0, 1164841200000)",
        "(41, 0, '', '', 41, 'Bronze Chamber', 50000000, 0, 1164841200000)",
        "(42, 0, '', '', 42, 'Golden Chamber', 50000000, 0, 1164841200000)",
        "(43, 0, '', '', 43, 'Silver Chamber', 50000000, 0, 1164841200000)",
        "(44, 0, '', '', 44, 'Mithril Chamber', 50000000, 0, 1164841200000)",
        "(45, 0, '', '', 45, 'Bronze Chamber', 50000000, 0, 1164841200000)",
        "(46, 0, '', '', 46, 'Silver Manor', 50000000, 0, 1164841200000)",
        "(47, 0, '', '', 47, 'Moonstone Hall', 50000000, 0, 1164841200000)",
        "(48, 0, '', '', 48, 'Onyx Hall', 50000000, 0, 1164841200000)",
        "(49, 0, '', '', 49, 'Emerald Hall', 50000000, 0, 1164841200000)",
        "(50, 0, '', '', 50, 'Sapphire Hall', 50000000, 0, 1164841200000)",
        "(51, 0, '', '', 51, 'Mont Chamber', 50000000, 0, 1164841200000)",
        "(52, 0, '', '', 52, 'Astaire Chamber', 50000000, 0, 1164841200000)",
        "(53, 0, '', '', 53, 'Aria Chamber', 50000000, 0, 1164841200000)",
        "(54, 0, '', '', 54, 'Yiana Chamber', 50000000, 0, 1164841200000)",
        "(55, 0, '', '', 55, 'Roien Chamber', 50000000, 0, 1164841200000)",
        "(56, 0, '', '', 56, 'Luna Chamber', 50000000, 0, 1164841200000)",
        "(57, 0, '', '', 57, 'Traban Chamber', 50000000, 0, 1164841200000)",
        "(58, 0, '', '', 58, 'Eisen Hall', 50000000, 0, 1164841200000)",
        "(59, 0, '', '', 59, 'Heavy Metal Hall', 50000000, 0, 1164841200000)",
        "(60, 0, '', '', 60, 'Molten Ore Hall', 50000000, 0, 1164841200000)",
        "(61, 0, '', '', 61, 'Titan Hall', 50000000, 0, 1164841200000)"
    )

    private val ItemInitDataId = intArrayOf(
        22,
        23,
        24,
        25,
        26,
        27,
        28,
        29,
        30,
        31,
        32,
        33,
        36,
        37,
        38,
        39,
        40,
        41,
        42,
        43,
        44,
        45,
        46,
        47,
        48,
        49,
        50,
        51,
        52,
        53,
        54,
        55,
        56,
        57,
        58,
        59,
        60,
        61
    )
    val auctions: List<Auction>
        get() = _auctions

    init {
        _auctions = ArrayList()
        load()
    }

    fun reload() {
        _auctions.clear()
        load()
    }

    private fun load() {
        try {
            L2DatabaseFactory.connection.use { con ->
                val statement = con.prepareStatement("SELECT id FROM auction ORDER BY id")
                val rs = statement.executeQuery()

                while (rs.next())
                    _auctions.add(Auction(rs.getInt("id")))

                rs.close()
                statement.close()

                _log.info("AuctionManager: Loaded " + auctions.size + " auction(s)")
            }
        } catch (e: Exception) {
            _log.log(Level.WARNING, "AuctionManager: an exception occured at auction.sql loading: " + e.message, e)
        }

    }

    fun getAuction(auctionId: Int): Auction? {
        val index = getAuctionIndex(auctionId)
        return if (index >= 0) auctions[index] else null

    }

    fun getAuctionIndex(auctionId: Int): Int {
        var auction: Auction?
        for (i in 0 until auctions.size) {
            auction = auctions[i]
            if (auction.id == auctionId)
                return i
        }
        return -1
    }

    /**
     * Init Clan NPC aution
     * @param id
     */
    fun initNPC(id: Int) {
        var i = 0
        while (i < ItemInitDataId.size) {
            if (ItemInitDataId[i] == id)
                break
            i++
        }

        if (i >= ItemInitDataId.size || ItemInitDataId[i] != id) {
            _log.warning("ClanHall auction not found for Id: $id")
            return
        }

        try {
            L2DatabaseFactory.connection.use { con ->
                val statement = con.prepareStatement("INSERT INTO `auction` VALUES " + ITEM_INIT_DATA[i])
                statement.execute()
                statement.close()
                _auctions.add(Auction(id))
            }
        } catch (e: Exception) {
            _log.log(Level.SEVERE, "AuctionManager: an exception occured at initNPC loading: " + e.message, e)
        }
    }
}