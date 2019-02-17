package com.l2kt.gameserver.model.entity

import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.instancemanager.AuctionManager
import com.l2kt.gameserver.instancemanager.ClanHallManager
import com.l2kt.gameserver.model.actor.instance.Door
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.model.zone.type.ClanHallZone
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.PledgeShowInfoUpdate
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import java.util.logging.Logger

class ClanHall(
    /**
     * @return clanHall id
     */
    val id: Int,
    /**
     * @return clanHall name
     */
    val name: String, ownerId: Int,
    /**
     * @return clanHall lease
     */
    val lease: Int,
    /**
     * @return clanHall description
     */
    val desc: String,
    /**
     * @return clanHall location
     */
    val location: String, paidUntil: Long,
    /**
     * @return clanHall grade
     */
    val grade: Int, paid: Boolean
) {
    /**
     * @return clanHall doors list.
     */
    val doors: List<Door> = ArrayList()
    private val _functions = ConcurrentHashMap<Int, ClanHallFunction>()

    /**
     * @return clanHall owner's id
     */
    var ownerId: Int = 0
        private set
    /**
     * @return the zone of this clan hall
     */
    /**
     * Sets this clan halls zone
     * @param zone
     */
    var zone: ClanHallZone? = null

    /**
     * @return time that clanHall is paid on.
     */
    var paidUntil: Long = 0
        protected set
    /**
     * @return if clanHall is paid or not
     */
    var paid: Boolean = false
        protected set
    protected var _isFree = true

    inner class ClanHallFunction(
        val type: Int,
        var lvl: Int,
        var lease: Int,
        protected var _tempFee: Int,
        val rate: Long,
        var endTime: Long,
        cwh: Boolean
    ) {
        protected var _inDebt: Boolean = false
        var _cwh: Boolean =
            false // first activating clanhall function is paid from player inventory, any others from clan warehouse

        init {
            initializeTask(cwh)
        }

        private fun initializeTask(cwh: Boolean) {
            if (_isFree)
                return

            val currentTime = System.currentTimeMillis()
            if (endTime > currentTime)
                ThreadPool.schedule(FunctionTask(cwh), endTime - currentTime)
            else
                ThreadPool.execute(FunctionTask(cwh))
        }

        private inner class FunctionTask(cwh: Boolean) : Runnable {
            init {
                _cwh = cwh
            }

            override fun run() {
                try {
                    if (_isFree)
                        return

                    val clan = ClanTable.getClan(ownerId)
                    if (clan!!.warehouse.adena >= lease || !_cwh) {
                        val fee = if (endTime == -1L) _tempFee else lease

                        endTime = System.currentTimeMillis() + rate
                        dbSave()

                        if (_cwh)
                            clan.warehouse.destroyItemByItemId("CH_function_fee", 57, fee, null, null)

                        ThreadPool.schedule(FunctionTask(true), rate)
                    } else
                        removeFunction(type)
                } catch (e: Exception) {
                    _log.log(Level.SEVERE, "", e)
                }

            }
        }

        fun dbSave() {
            try {
                L2DatabaseFactory.connection.use { con ->
                    val statement =
                        con.prepareStatement("REPLACE INTO clanhall_functions (hall_id, type, lvl, lease, rate, endTime) VALUES (?,?,?,?,?,?)")
                    statement.setInt(1, id)
                    statement.setInt(2, type)
                    statement.setInt(3, lvl)
                    statement.setInt(4, lease)
                    statement.setLong(5, rate)
                    statement.setLong(6, endTime)
                    statement.execute()
                    statement.close()
                }
            } catch (e: Exception) {
                _log.log(
                    Level.SEVERE,
                    "Exception: ClanHall.updateFunctions(int type, int lvl, int lease, long rate, long time, boolean addNew): " + e.message,
                    e
                )
            }

        }
    }

    init {
        this.ownerId = ownerId
        this.paidUntil = paidUntil
        this.paid = paid

        if (ownerId != 0) {
            _isFree = false

            initializeTask(false)
            loadFunctions()
        }
    }

    /**
     * @param doorId The id to make checks on.
     * @return a doorInstance based on a doorId.
     */
    fun getDoor(doorId: Int): Door? {
        for (door in doors) {
            if (door.doorId == doorId)
                return door
        }
        return null
    }

    /**
     * @param type
     * @return clanHall function with id
     */
    fun getFunction(type: Int): ClanHallFunction {
        return _functions[type]!!
    }

    /** Free this clan hall  */
    fun free() {
        ownerId = 0
        _isFree = true
        paidUntil = 0
        paid = false

        removeAllFunctions()
        updateDb()
    }

    /**
     * Set owner if clan hall is free
     * @param clan The new clan owner.
     */
    fun setOwner(clan: Clan?) {
        // Verify that this ClanHall is Free and Clan isn't null
        if (ownerId > 0 || clan == null)
            return

        ownerId = clan.clanId
        _isFree = false
        paidUntil = System.currentTimeMillis()

        initializeTask(true)

        // Annonce to Online member new ClanHall
        clan.broadcastToOnlineMembers(PledgeShowInfoUpdate(clan))
        updateDb()
    }

    /**
     * Open or close a door.
     * @param activeChar The player who request to open/close the door.
     * @param doorId The affected doorId.
     * @param open true will open it, false will close.
     */
    fun openCloseDoor(activeChar: Player?, doorId: Int, open: Boolean) {
        if (activeChar != null && activeChar.clanId == ownerId)
            openCloseDoor(doorId, open)
    }

    fun openCloseDoor(doorId: Int, open: Boolean) {
        openCloseDoor(getDoor(doorId), open)
    }

    fun openCloseDoors(activeChar: Player?, open: Boolean) {
        if (activeChar != null && activeChar.clanId == ownerId)
            openCloseDoors(open)
    }

    fun openCloseDoors(open: Boolean) {
        for (door in doors) {
            if (open)
                door.openMe()
            else
                door.closeMe()
        }
    }

    /** Banish Foreigner  */
    fun banishForeigners() {
        zone!!.banishForeigners(ownerId)
    }

    /** Load All Functions  */
    private fun loadFunctions() {
        try {
            L2DatabaseFactory.connection.use { con ->
                val statement = con.prepareStatement("Select * from clanhall_functions where hall_id = ?")
                statement.setInt(1, id)
                val rs = statement.executeQuery()
                while (rs.next()) {
                    _functions[rs.getInt("type")] = ClanHallFunction(
                        rs.getInt("type"),
                        rs.getInt("lvl"),
                        rs.getInt("lease"),
                        0,
                        rs.getLong("rate"),
                        rs.getLong("endTime"),
                        true
                    )
                }
                rs.close()
                statement.close()
            }
        } catch (e: Exception) {
            _log.log(Level.SEVERE, "Exception: ClanHall.loadFunctions(): " + e.message, e)
        }

    }

    /**
     * Remove function In List and in DB
     * @param functionType the function id to remove.
     */
    fun removeFunction(functionType: Int) {
        _functions.remove(functionType)

        try {
            L2DatabaseFactory.connection.use { con ->
                val statement = con.prepareStatement("DELETE FROM clanhall_functions WHERE hall_id=? AND type=?")
                statement.setInt(1, id)
                statement.setInt(2, functionType)
                statement.execute()
                statement.close()
            }
        } catch (e: Exception) {
            _log.log(Level.SEVERE, "Exception: ClanHall.removeFunction(int functionType): " + e.message, e)
        }

    }

    /**
     * Remove all functions linked to a particular Clan Hall.
     */
    fun removeAllFunctions() {
        _functions.clear()

        try {
            L2DatabaseFactory.connection.use { con ->
                val statement = con.prepareStatement("DELETE FROM clanhall_functions WHERE hall_id=?")
                statement.setInt(1, id)
                statement.execute()
                statement.close()
            }
        } catch (e: Exception) {
            _log.log(Level.SEVERE, "Exception: ClanHall.removeAllFunctions(): " + e.message, e)
        }

    }

    /**
     * Update Function of a clanHall.
     * @param player The player who requested the change.
     * @param type
     * @param lvl
     * @param lease
     * @param rate
     * @param addNew
     * @return
     */
    fun updateFunctions(player: Player?, type: Int, lvl: Int, lease: Int, rate: Long, addNew: Boolean): Boolean {
        if (player == null)
            return false

        if (lease > 0) {
            if (!player.destroyItemByItemId("Consume", 57, lease, null, true))
                return false
        }

        if (addNew)
            _functions[type] = ClanHallFunction(type, lvl, lease, 0, rate, 0, false)
        else {
            if (lvl == 0 && lease == 0)
                removeFunction(type)
            else {
                val chf = _functions[type]!!

                val diffLease = lease - chf.lease
                if (diffLease > 0)
                    _functions[type] = ClanHallFunction(type, lvl, lease, diffLease, rate, -1, false)
                else {
                    chf.lease = lease
                    chf.lvl = lvl
                    chf.dbSave()
                }
            }
        }
        return true
    }

    /** Update DB  */
    fun updateDb() {
        try {
            L2DatabaseFactory.connection.use { con ->
                val statement = con.prepareStatement("UPDATE clanhall SET ownerId=?, paidUntil=?, paid=? WHERE id=?")
                statement.setInt(1, ownerId)
                statement.setLong(2, paidUntil)
                statement.setInt(3, if (paid) 1 else 0)
                statement.setInt(4, id)
                statement.execute()
                statement.close()
            }
        } catch (e: Exception) {
            _log.log(Level.WARNING, "Exception: updateOwnerInDB(L2Clan clan): " + e.message, e)
        }

    }

    /**
     * Initialize Fee Task
     * @param forced
     */
    private fun initializeTask(forced: Boolean) {
        val currentTime = System.currentTimeMillis()
        if (paidUntil > currentTime)
            ThreadPool.schedule(FeeTask(), paidUntil - currentTime)
        else if (!paid && !forced) {
            if (System.currentTimeMillis() + 86400000 <= paidUntil + CH_RATE)
                ThreadPool.schedule(FeeTask(), System.currentTimeMillis() + 86400000)
            else
                ThreadPool.schedule(FeeTask(), paidUntil + CH_RATE - System.currentTimeMillis())
        } else
            ThreadPool.schedule(FeeTask(), 0)
    }

    /** Fee Task  */
    private inner class FeeTask : Runnable {

        override fun run() {
            try {
                val time = System.currentTimeMillis()

                if (_isFree)
                    return

                if (paidUntil > time) {
                    ThreadPool.schedule(FeeTask(), paidUntil - time)
                    return
                }

                val clan = ClanTable.getClan(ownerId)
                if (clan!!.warehouse.adena >= lease) {
                    if (paidUntil != 0L) {
                        while (paidUntil <= time)
                            paidUntil += CH_RATE.toLong()
                    } else
                        paidUntil = time + CH_RATE

                    clan.warehouse.destroyItemByItemId("CH_rental_fee", 57, lease, null, null)

                    ThreadPool.schedule(FeeTask(), paidUntil - time)
                    paid = true
                    updateDb()
                } else {
                    paid = false

                    if (time > paidUntil + CH_RATE) {
                        if (ClanHallManager.loaded()) {
                            AuctionManager.initNPC(id)
                            ClanHallManager.setFree(id)
                            clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED))
                        } else
                            ThreadPool.schedule(FeeTask(), 3000)
                    } else {
                        updateDb()
                        clan.broadcastToOnlineMembers(
                            SystemMessage.getSystemMessage(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW).addNumber(
                                lease
                            )
                        )

                        if (time + 86400000 <= paidUntil + CH_RATE)
                            ThreadPool.schedule(FeeTask(), time + 86400000)
                        else
                            ThreadPool.schedule(FeeTask(), paidUntil + CH_RATE - time)
                    }
                }
            } catch (e: Exception) {
                _log.log(Level.SEVERE, "", e)
            }

        }
    }

    companion object {
        protected val _log = Logger.getLogger(ClanHall::class.java.name)

        private const val CH_RATE = 604800000

        const val FUNC_TELEPORT = 1
        const val FUNC_ITEM_CREATE = 2
        const val FUNC_RESTORE_HP = 3
        const val FUNC_RESTORE_MP = 4
        const val FUNC_RESTORE_EXP = 5
        const val FUNC_SUPPORT = 6
        const val FUNC_DECO_FRONTPLATEFORM = 7
        const val FUNC_DECO_CURTAINS = 8

        fun openCloseDoor(door: Door?, open: Boolean) {
            if (door != null) {
                if (open)
                    door.openMe()
                else
                    door.closeMe()
            }
        }
    }
}