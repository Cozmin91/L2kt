package com.l2kt.gameserver.model.buylist

import com.l2kt.L2DatabaseFactory
import com.l2kt.gameserver.data.ItemTable
import com.l2kt.gameserver.model.item.kind.Item
import com.l2kt.gameserver.taskmanager.BuyListTaskManager
import com.l2kt.gameserver.templates.StatsSet
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Level
import java.util.logging.Logger

/**
 * A datatype entry for [NpcBuyList]. It can own a count and a restock delay, the whole system of tasks being controlled by [BuyListTaskManager].
 */
class Product(val buyListId: Int, set: StatsSet) {
    val item: Item = ItemTable.getTemplate(set.getInteger("id"))!!
    val price: Int = set.getInteger("price", 0)
    val restockDelay: Long = set.getLong("restockDelay", -1) * 60000
    val maxCount: Int = set.getInteger("count", -1)
    private var _count: AtomicInteger? = null

    val itemId: Int
        get() = item.itemId

    /**
     * Get the actual [Product] count.<br></br>
     * If this Product doesn't own a timer (valid if _maxCount > -1), return 0.
     * @return the actual Product count.
     */
    /**
     * Set arbitrarily the current amount of a [Product].
     * @param currentCount : The amount to set.
     */
    var count: Int
        get() {
            if (_count == null)
                return 0

            val count = _count!!.get()
            return if (count > 0) count else 0
        }
        set(currentCount) = _count!!.set(currentCount)

    init {

        if (hasLimitedStock())
            _count = AtomicInteger(maxCount)
    }

    /**
     * Decrease [Product] count, but only if result is superior or equal to 0, and if _count exists.<br></br>
     * We setup this Product in the general task if not already existing, and save result on database.
     * @param val : The value to decrease.
     * @return true if the Product count can be reduced ; false otherwise.
     */
    fun decreaseCount(`val`: Int): Boolean {
        if (_count == null)
            return false

        // We test product addition and save result, but only if count has been affected.
        val result = _count!!.addAndGet(-`val`) >= 0
        if (result)
            BuyListTaskManager.add(this, restockDelay)

        return result
    }

    fun hasLimitedStock(): Boolean {
        return maxCount > -1
    }

    /**
     * Save the [Product] into database. Happens on successful decrease count.
     * @param nextRestockTime : The new restock timer.
     */
    fun save(nextRestockTime: Long) {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(ADD_OR_UPDATE_BUYLIST).use { ps ->
                    ps.setInt(1, buyListId)
                    ps.setInt(2, itemId)
                    ps.setInt(3, count)
                    ps.setLong(4, nextRestockTime)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            LOG.log(Level.SEVERE, "Failed to save product for buylist id:$buyListId and item id:$itemId", e)
        }

    }

    /**
     * Delete the [Product] from database. Happens on restock time reset.
     */
    fun delete() {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(DELETE_BUYLIST).use { ps ->
                    ps.setInt(1, buyListId)
                    ps.setInt(2, itemId)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            LOG.log(Level.SEVERE, "Failed to save product for buylist id:$buyListId and item id:$itemId", e)
        }

    }

    companion object {
        private val LOG = Logger.getLogger(Product::class.java.name)

        private const val ADD_OR_UPDATE_BUYLIST =
            "INSERT INTO buylists (buylist_id,item_id,count,next_restock_time) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE count=VALUES(count), next_restock_time=VALUES(next_restock_time)"
        private const val DELETE_BUYLIST = "DELETE FROM buylists WHERE buylist_id=? AND item_id=?"
    }
}