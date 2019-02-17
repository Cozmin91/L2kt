package com.l2kt.gameserver.model.itemcontainer

import com.l2kt.Config
import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.logging.CLogger
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.ItemTable
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import java.util.*
import java.util.concurrent.ConcurrentSkipListSet

abstract class ItemContainer protected constructor() {

    protected val _items: MutableSet<ItemInstance> = ConcurrentSkipListSet()

    protected abstract val owner: Creature?

    protected abstract val baseLocation: ItemInstance.ItemLocation

    open val name: String
        get() = "ItemContainer"

    /**
     * @return the owner objectId of the inventory.
     */
    open val ownerId: Int
        get() = if (owner == null) 0 else owner!!.objectId

    /**
     * @return the quantity of items in the inventory.
     */
    open val size: Int
        get() = _items.size

    /**
     * @return the list of items in inventory.
     */
    open val items: Set<ItemInstance>
        get() = _items

    /**
     * @return the amount of adena (itemId 57)
     */
    open val adena: Int
        get() {
            for (item in _items) {
                if (item.itemId == 57)
                    return item.count
            }
            return 0
        }

    /**
     * Check for multiple items in player's inventory.
     * @param itemIds a list of item Ids to check.
     * @return true if at least one items exists in player's inventory, false otherwise
     */
    fun hasAtLeastOneItem(vararg itemIds: Int): Boolean {
        for (itemId in itemIds) {
            if (getItemByItemId(itemId) != null)
                return true
        }
        return false
    }

    /**
     * @param itemId : the itemId to check.
     * @return a List holding the items list (empty list if not found)
     */
    fun getItemsByItemId(itemId: Int): List<ItemInstance> {
        val list = ArrayList<ItemInstance>()
        for (item in _items) {
            if (item.itemId == itemId)
                list.add(item)
        }
        return list
    }

    /**
     * @param itemId : the itemId to check.
     * @return the item by using its itemId, or null if not found in inventory.
     */
    open fun getItemByItemId(itemId: Int): ItemInstance? {
        for (item in _items) {
            if (item.itemId == itemId)
                return item
        }
        return null
    }

    /**
     * @param objectId : the objectId to check.
     * @return the item by using its objectId, or null if not found in inventory
     */
    fun getItemByObjectId(objectId: Int): ItemInstance? {
        for (item in _items) {
            if (item.objectId == objectId)
                return item
        }
        return null
    }

    /**
     * @param itemId : the itemId to check.
     * @param enchantLevel : enchant level to match on, or -1 for ANY enchant level.
     * @param includeEquipped : include equipped items.
     * @return the count of items matching the above conditions.
     */
    @JvmOverloads
    fun getInventoryItemCount(itemId: Int, enchantLevel: Int, includeEquipped: Boolean = true): Int {
        var count = 0

        for (item in _items) {
            if (item.itemId == itemId && (item.enchantLevel == enchantLevel || enchantLevel < 0) && (includeEquipped || !item.isEquipped)) {
                if (item.isStackable)
                    return item.count

                count++
            }
        }
        return count
    }

    /**
     * Adds item to inventory
     * @param process : String identifier of process triggering this action.
     * @param item : ItemInstance to add.
     * @param actor : The player requesting the item addition.
     * @param reference : The WorldObject referencing current action (like NPC selling item or previous item in transformation,...)
     * @return the ItemInstance corresponding to the new or updated item.
     */
    open fun addItem(process: String, item: ItemInstance, actor: Player?, reference: WorldObject?): ItemInstance? {
        var item = item
        val olditem = getItemByItemId(item.itemId)

        // If stackable item is found in inventory just add to current quantity
        if (olditem != null && olditem.isStackable) {
            val count = item.count
            olditem.changeCount(process, count, actor, reference)
            olditem.lastChange = ItemInstance.ItemState.MODIFIED

            // And destroys the item
            item.destroyMe(process, actor, reference)
            item.updateDatabase()
            item = olditem

            // Updates database
            if (item.itemId == 57 && count < 10000 * Config.RATE_DROP_ADENA) {
                // Small adena changes won't be saved to database all the time
                if (Rnd[10] < 2)
                    item.updateDatabase()
            } else
                item.updateDatabase()
        } else {
            item.setOwnerId(process, ownerId, actor, reference)
            item.location = baseLocation
            item.lastChange = ItemInstance.ItemState.ADDED

            // Add item in inventory
            addItem(item)

            // Updates database
            item.updateDatabase()
        }// If item hasn't be found in inventory, create new one

        refreshWeight()
        return item
    }

    /**
     * Adds an item to inventory.
     * @param process : String identifier of process triggering this action.
     * @param itemId : The itemId of the ItemInstance to add.
     * @param count : The quantity of items to add.
     * @param actor : The player requesting the item addition.
     * @param reference : The WorldObject referencing current action (like NPC selling item or previous item in transformation,...)
     * @return the ItemInstance corresponding to the new or updated item.
     */
    open fun addItem(process: String, itemId: Int, count: Int, actor: Player?, reference: WorldObject?): ItemInstance? {
        var item = getItemByItemId(itemId)

        // If stackable item is found in inventory just add to current quantity
        if (item != null && item.isStackable) {
            item.changeCount(process, count, actor, reference)
            item.lastChange = ItemInstance.ItemState.MODIFIED

            // Updates database
            if (itemId == 57 && count < 10000 * Config.RATE_DROP_ADENA) {
                // Small adena changes won't be saved to database all the time
                if (Rnd[10] < 2)
                    item.updateDatabase()
            } else
                item.updateDatabase()
        } else {
            val template = ItemTable.getTemplate(itemId) ?: return null

            for (i in 0 until count) {
                item = ItemInstance.create(itemId, if (template.isStackable) count else 1, actor, reference)
                item!!.ownerId = ownerId
                item.location = baseLocation
                item.lastChange = ItemInstance.ItemState.ADDED

                // Add item in inventory
                addItem(item)

                // Updates database
                item.updateDatabase()

                // If stackable, end loop as entire count is included in 1 instance of item
                if (template.isStackable || !Config.MULTIPLE_ITEM_DROP)
                    break
            }
        }// If item hasn't be found in inventory, create new one

        refreshWeight()
        return item
    }

    /**
     * Transfers item to another inventory
     * @param process : String Identifier of process triggering this action
     * @param objectId : int objectid of the item to be transfered
     * @param count : int Quantity of items to be transfered
     * @param target
     * @param actor : Player Player requesting the item transfer
     * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @return ItemInstance corresponding to the new item or the updated item in inventory
     */
    open fun transferItem(
        process: String,
        objectId: Int,
        count: Int,
        target: ItemContainer?,
        actor: Player?,
        reference: WorldObject?
    ): ItemInstance? {
        var count = count
        if (target == null)
            return null

        val sourceitem = getItemByObjectId(objectId) ?: return null

        var targetitem: ItemInstance? = if (sourceitem.isStackable) target.getItemByItemId(sourceitem.itemId) else null

        synchronized(sourceitem) {
            // check if this item still present in this container
            if (getItemByObjectId(objectId) != sourceitem)
                return null

            // Check if requested quantity is available
            if (count > sourceitem.count)
                count = sourceitem.count

            // If possible, move entire item object
            if (sourceitem.count == count && targetitem == null) {
                removeItem(sourceitem)
                target.addItem(process, sourceitem, actor, reference)
                targetitem = sourceitem
            } else {
                if (sourceitem.count > count)
                // If possible, only update counts
                    sourceitem.changeCount(process, -count, actor, reference)
                else
                // Otherwise destroy old item
                {
                    removeItem(sourceitem)
                    sourceitem.destroyMe(process, actor, reference)
                }

                if (targetitem != null)
                // If possible, only update counts
                    targetitem!!.changeCount(process, count, actor, reference)
                else
                // Otherwise add new item
                    targetitem = target.addItem(process, sourceitem.itemId, count, actor, reference)
            }

            // Updates database
            sourceitem.updateDatabase()

            if (targetitem != sourceitem && targetitem != null)
                targetitem!!.updateDatabase()

            if (sourceitem.isAugmented)
                sourceitem.getAugmentation()!!.removeBonus(actor)

            refreshWeight()
            target.refreshWeight()
        }
        return targetitem
    }

    /**
     * Destroy item from inventory and updates database
     * @param process : String Identifier of process triggering this action
     * @param item : ItemInstance to be destroyed
     * @param actor : Player Player requesting the item destroy
     * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @return ItemInstance corresponding to the destroyed item or the updated item in inventory
     */
    open fun destroyItem(process: String, item: ItemInstance, actor: Player?, reference: WorldObject?): ItemInstance? {
        return destroyItem(process, item, item.count, actor, reference)
    }

    /**
     * Destroy item from inventory and updates database
     * @param process : String Identifier of process triggering this action
     * @param item : ItemInstance to be destroyed
     * @param count
     * @param actor : Player Player requesting the item destroy
     * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @return ItemInstance corresponding to the destroyed item or the updated item in inventory
     */
    open fun destroyItem(
        process: String?,
        item: ItemInstance,
        count: Int,
        actor: Player?,
        reference: WorldObject?
    ): ItemInstance? {
        synchronized(item) {
            // Adjust item quantity
            if (item.count > count) {
                item.changeCount(process, -count, actor, reference)
                item.lastChange = ItemInstance.ItemState.MODIFIED

                // don't update often for untraced items
                if (process != null || Rnd[10] == 0)
                    item.updateDatabase()

                refreshWeight()

                return item
            }

            if (item.count < count)
                return null

            val removed = removeItem(item)
            if (!removed)
                return null

            item.destroyMe(process, actor, reference)

            item.updateDatabase()
            refreshWeight()
        }
        return item
    }

    /**
     * Destroy item from inventory by using its <B>objectID</B> and updates database
     * @param process : String Identifier of process triggering this action
     * @param objectId : int Item Instance identifier of the item to be destroyed
     * @param count : int Quantity of items to be destroyed
     * @param actor : Player Player requesting the item destroy
     * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @return ItemInstance corresponding to the destroyed item or the updated item in inventory
     */
    open fun destroyItem(
        process: String,
        objectId: Int,
        count: Int,
        actor: Player?,
        reference: WorldObject?
    ): ItemInstance? {
        val item = getItemByObjectId(objectId) ?: return null

        return destroyItem(process, item, count, actor, reference)
    }

    /**
     * Destroy item from inventory by using its <B>itemId</B> and updates database
     * @param process : String Identifier of process triggering this action
     * @param itemId : int Item identifier of the item to be destroyed
     * @param count : int Quantity of items to be destroyed
     * @param actor : Player Player requesting the item destroy
     * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @return ItemInstance corresponding to the destroyed item or the updated item in inventory
     */
    open fun destroyItemByItemId(
        process: String,
        itemId: Int,
        count: Int,
        actor: Player?,
        reference: WorldObject?
    ): ItemInstance? {
        val item = getItemByItemId(itemId) ?: return null

        return destroyItem(process, item, count, actor, reference)
    }

    /**
     * Destroy all items from inventory and updates database
     * @param process : String Identifier of process triggering this action
     * @param actor : Player Player requesting the item destroy
     * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
     */
    fun destroyAllItems(process: String, actor: Player?, reference: WorldObject?) {
        for (item in _items)
            destroyItem(process, item, actor, reference)
    }

    /**
     * Adds item to inventory for further adjustments.
     * @param item : ItemInstance to be added from inventory
     */
    protected open fun addItem(item: ItemInstance) {
        item.actualizeTime()

        _items.add(item)
    }

    /**
     * Removes item from inventory for further adjustments.
     * @param item : ItemInstance to be removed from inventory
     * @return
     */
    protected open fun removeItem(item: ItemInstance): Boolean {
        return _items.remove(item)
    }

    /**
     * Refresh the weight of equipment loaded
     */
    protected open fun refreshWeight() {}

    /**
     * Delete item object from world
     */
    open fun deleteMe() {
        if (owner != null) {
            for (item in _items) {
                item.updateDatabase()
                World.removeObject(item)
            }
        }
        _items.clear()
    }

    /**
     * Update database with items in inventory
     */
    fun updateDatabase() {
        if (owner != null) {
            for (item in _items)
                item.updateDatabase()
        }
    }

    /**
     * Get back items in container from database
     */
    open fun restore() {
        val owner = if (owner == null) null else owner!!.actingPlayer

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(RESTORE_ITEMS).use { ps ->
                    ps.setInt(1, ownerId)
                    ps.setString(2, baseLocation.name)

                    ps.executeQuery().use { rs ->
                        while (rs.next()) {
                            // Restore the item.
                            val item = ItemInstance.restoreFromDb(ownerId, rs) ?: continue

                            // Add the item to world objects list.
                            World.addObject(item)

                            // If stackable item is found in inventory just add to current quantity
                            if (item.isStackable && getItemByItemId(item.itemId) != null)
                                addItem("Restore", item, owner, null)
                            else
                                addItem(item)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't restore container for {}.", e, ownerId)
        }

        refreshWeight()
    }

    open fun validateCapacity(slots: Int): Boolean {
        return true
    }

    open fun validateWeight(weight: Int): Boolean {
        return true
    }

    companion object {
        val LOGGER = CLogger(ItemContainer::class.java.name)

        private const val RESTORE_ITEMS =
            "SELECT object_id, item_id, count, enchant_level, loc, loc_data, custom_type1, custom_type2, mana_left, time FROM items WHERE owner_id=? AND (loc=?)"
    }
}
/**
 * @param itemId : the itemId to check.
 * @param enchantLevel : enchant level to match on, or -1 for ANY enchant level.
 * @return int corresponding to the number of items matching the above conditions.
 */