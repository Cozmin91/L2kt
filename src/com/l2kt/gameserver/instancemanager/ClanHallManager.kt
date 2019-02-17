package com.l2kt.gameserver.instancemanager

import com.l2kt.L2DatabaseFactory
import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.model.entity.ClanHall
import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.model.zone.type.ClanHallZone
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author Steuf
 */
object ClanHallManager {

    private val _allClanHalls: MutableMap<String, MutableList<ClanHall>>
    private val _clanHall: MutableMap<Int, ClanHall>
    private val _freeClanHall: MutableMap<Int, ClanHall>
    private var _loaded = false
    val _log = Logger.getLogger(ClanHallManager::class.java.name)

    /**
     * @return Map with all free ClanHalls
     */
    val freeClanHalls: Map<Int, ClanHall>
        get() = _freeClanHall

    /**
     * @return Map with all ClanHalls that have owner
     */
    val clanHalls: Map<Int, ClanHall>
        get() = _clanHall

    fun loaded(): Boolean {
        return _loaded
    }

    init {
        _allClanHalls = HashMap()
        _clanHall = HashMap()
        _freeClanHall = HashMap()
        load()
    }

    /** Load All Clan Hall  */
    private fun load() {
        try {
            L2DatabaseFactory.connection.use { con ->
                var id: Int
                var ownerId: Int
                var lease: Int
                var grade = 0
                var Name: String
                var Desc: String
                var Location: String
                var paidUntil: Long = 0
                var paid = false

                val statement = con.prepareStatement("SELECT * FROM clanhall ORDER BY id")
                val rs = statement.executeQuery()
                while (rs.next()) {
                    id = rs.getInt("id")
                    Name = rs.getString("name")
                    ownerId = rs.getInt("ownerId")
                    lease = rs.getInt("lease")
                    Desc = rs.getString("desc")
                    Location = rs.getString("location")
                    paidUntil = rs.getLong("paidUntil")
                    grade = rs.getInt("Grade")
                    paid = rs.getBoolean("paid")

                    val ch = ClanHall(id, Name, ownerId, lease, Desc, Location, paidUntil, grade, paid)

                    if (!_allClanHalls.containsKey(Location))
                        _allClanHalls[Location] = ArrayList()

                    _allClanHalls[Location]?.add(ch)

                    if (ownerId > 0) {
                        val owner = ClanTable.getClan(ownerId)
                        if (owner != null) {
                            _clanHall[id] = ch
                            owner.setHideout(id)
                            continue
                        }
                        ch.free()
                    }
                    _freeClanHall[id] = ch

                    val auc = AuctionManager.getAuction(id)
                    if (auc == null && lease > 0)
                        AuctionManager.initNPC(id)
                }
                rs.close()
                statement.close()

                _log.info("ClanHallManager: Loaded " + clanHalls.size + " clan halls.")
                _log.info("ClanHallManager: Loaded " + freeClanHalls.size + " free clan halls.")
                _loaded = true
            }
        } catch (e: Exception) {
            _log.log(Level.WARNING, "Exception: ClanHallManager.load(): " + e.message, e)
        }

    }

    /**
     * @param location
     * @return Map with all ClanHalls which are in location
     */
    fun getClanHallsByLocation(location: String): List<ClanHall>? {
        return if (!_allClanHalls.containsKey(location)) null else _allClanHalls[location]

    }

    /**
     * @param chId the clanHall id to check.
     * @return true if the clanHall is free.
     */
    fun isFree(chId: Int): Boolean {
        return _freeClanHall.containsKey(chId)
    }

    /**
     * Free a ClanHall
     * @param chId the id of clanHall to release.
     */
    @Synchronized
    fun setFree(chId: Int) {
        _freeClanHall[chId] = _clanHall[chId]!!
        ClanTable.getClan(_freeClanHall[chId]!!.ownerId)!!.setHideout(0)
        _freeClanHall[chId]?.free()
        _clanHall.remove(chId)
    }

    /**
     * Set owner status for a clan hall.
     * @param chId the clanHall id to make checks on.
     * @param clan the new clan owner.
     */
    @Synchronized
    fun setOwner(chId: Int, clan: Clan) {
        if (!_clanHall.containsKey(chId)) {
            _clanHall[chId] = _freeClanHall[chId]!!
            _freeClanHall.remove(chId)
        } else
            _clanHall[chId]?.free()

        ClanTable.getClan(clan.clanId)!!.setHideout(chId)
        _clanHall[chId]?.setOwner(clan)
    }

    /**
     * @param clanHallId the id to use.
     * @return a clanHall by its id.
     */
    fun getClanHallById(clanHallId: Int): ClanHall? {
        if (_clanHall.containsKey(clanHallId))
            return _clanHall[clanHallId]

        if (_freeClanHall.containsKey(clanHallId))
            return _freeClanHall[clanHallId]

        _log.warning("ClanHall (id: $clanHallId) isn't found in clanhall table.")
        return null
    }

    fun getNearbyClanHall(x: Int, y: Int, maxDist: Int): ClanHall? {
        var zone: ClanHallZone? = null

        for ((_, value) in _clanHall) {
            zone = value.zone
            if (zone != null && zone.getDistanceToZone(x, y) < maxDist)
                return value
        }
        for ((_, value) in _freeClanHall) {
            zone = value.zone
            if (zone != null && zone.getDistanceToZone(x, y) < maxDist)
                return value
        }
        return null
    }

    /**
     * @param clan the clan to use.
     * @return a clanHall by its owner.
     */
    fun getClanHallByOwner(clan: Clan): ClanHall? {
        for ((_, value) in _clanHall) {
            if (clan.clanId == value.ownerId)
                return value
        }
        return null
    }
}