package com.l2kt.gameserver.taskmanager

import java.util.concurrent.ConcurrentHashMap

import com.l2kt.Config
import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.logging.CLogger
import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.data.manager.CursedWeaponManager
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.item.instance.ItemInstance

/**
 * Destroys item on ground after specified time. When server is about to shutdown/restart, saves all dropped items in to SQL. Loads them during server start.
 */
object ItemsOnGroundTaskManager : Runnable {

    private val items = ConcurrentHashMap<ItemInstance, Long>()
    private val LOGGER = CLogger(ItemsOnGroundTaskManager::class.java.name)

    private const val LOAD_ITEMS = "SELECT object_id,item_id,count,enchant_level,x,y,z,time FROM items_on_ground"
    private const val DELETE_ITEMS = "DELETE FROM items_on_ground"
    private const val SAVE_ITEMS = "INSERT INTO items_on_ground(object_id,item_id,count,enchant_level,x,y,z,time) VALUES(?,?,?,?,?,?,?,?)"

    init {
        ThreadPool.scheduleAtFixedRate(this, 5000, 5000)

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(LOAD_ITEMS).use { st ->
                    con.prepareStatement(DELETE_ITEMS).use { st2 ->
                        st.executeQuery().use { rs ->
                            val time = System.currentTimeMillis()

                            while (rs.next()) {
                                // Create new item.
                                val item = ItemInstance(rs.getInt(1), rs.getInt(2))
                                World.getInstance().addObject(item)

                                // Check and set count.
                                val count = rs.getInt(3)
                                if (item.isStackable && count > 1)
                                    item.count = count

                                // Check and set enchant.
                                val enchant = rs.getInt(4)
                                if (enchant > 0)
                                    item.enchantLevel = enchant

                                // Spawn item in the world.
                                item.spawnMe(rs.getInt(5), rs.getInt(6), rs.getInt(7))

                                // If item is on a Castle ground, verify if it's an allowed ticket. If yes, add it to associated castle.
                                val castle = CastleManager.getInstance().getCastle(item)
                                if (castle?.getTicket(item.itemId) != null)
                                    castle.addDroppedTicket(item)

                                // Get interval, add item to the list.
                                val interval = rs.getLong(8)
                                items[item] = if (interval == 0L) 0L else time + interval
                            }

                            // Delete all items from database.
                            st2.execute()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Error while loading items on ground data.", e)
        }

        LOGGER.info("Restored {} items on ground.", items.size)
    }

    override fun run() {
        if (items.isEmpty())
            return

        val time = System.currentTimeMillis()

        for ((item, destroyTime) in items) {
            if (destroyTime == 0L || time < destroyTime)
                continue

            item.decayMe()
        }
    }

    /**
     * Adds [ItemInstance] to the ItemAutoDestroyTask.
     * @param item : [ItemInstance] to be added and checked.
     * @param actor : [Creature] who dropped the item.
     */
    fun add(item: ItemInstance, actor: Creature) {
        // Actor doesn't exist or item is protected, don't bother with the item (e.g. Cursed Weapons).
        if (item.isDestroyProtected)
            return

        var dropTime: Long

        // Item has special destroy time, use it.
        val special = Config.SPECIAL_ITEM_DESTROY_TIME[item.itemId]
        dropTime = when {
            special != null -> special.toLong()
            item.isHerb -> Config.HERB_AUTO_DESTROY_TIME.toLong()
            item.isEquipable -> Config.EQUIPABLE_ITEM_AUTO_DESTROY_TIME.toLong()
            else -> {
                // If item is on a Castle ground, verify if it's an allowed ticket. If yes, the associated timer is always 0.
                val castle = CastleManager.getInstance().getCastle(item)
                (if (castle?.getTicket(item.itemId) != null) 0 else Config.ITEM_AUTO_DESTROY_TIME).toLong()
            }
        }

        // Item was dropped by playable, apply the multiplier.
        if (actor is Playable)
            dropTime *= Config.PLAYER_DROPPED_ITEM_MULTIPLIER.toLong()

        if (dropTime != 0L)
            dropTime += System.currentTimeMillis()

        items[item] = dropTime
    }

    fun remove(item: ItemInstance) {
        items.remove(item)
    }

    fun save() {
        if (items.isEmpty()) {
            LOGGER.info("No items on ground to save.")
            return
        }

        // Store whole items list to database.
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(SAVE_ITEMS).use { st ->
                    // Get current time.
                    val time = System.currentTimeMillis()

                    for ((item, left) in items) {
                        // Get item and destroy time interval.

                        // Cursed Items not saved to ground, prevent double save.
                        if (CursedWeaponManager.getInstance().isCursed(item.itemId))
                            continue

                        st.setInt(1, item.objectId)
                        st.setInt(2, item.itemId)
                        st.setInt(3, item.count)
                        st.setInt(4, item.enchantLevel)
                        st.setInt(5, item.x)
                        st.setInt(6, item.y)
                        st.setInt(7, item.z)

                        st.setLong(8, if (left == 0L) 0 else left - time)

                        st.addBatch()
                    }
                    st.executeBatch()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't save items on ground.", e)
        }

        LOGGER.info("Saved {} items on ground.", items.size)
    }
}