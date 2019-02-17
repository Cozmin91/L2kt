package com.l2kt.gameserver.model.itemcontainer

import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.item.type.EtcItemType
import com.l2kt.gameserver.model.itemcontainer.listeners.ArmorSetListener
import com.l2kt.gameserver.model.itemcontainer.listeners.BowRodListener
import com.l2kt.gameserver.model.itemcontainer.listeners.ItemPassiveSkillsListener
import com.l2kt.gameserver.model.tradelist.TradeItem
import com.l2kt.gameserver.model.tradelist.TradeList
import com.l2kt.gameserver.network.serverpackets.InventoryUpdate
import com.l2kt.gameserver.network.serverpackets.StatusUpdate
import com.l2kt.gameserver.taskmanager.ShadowItemTaskManager
import java.util.*

class PcInventory(public override val owner: Player) : Inventory() {
    var adenaInstance: ItemInstance? = null
        private set
    var ancientAdenaInstance: ItemInstance? = null
        private set

    override val baseLocation: ItemInstance.ItemLocation
        get() = ItemInstance.ItemLocation.INVENTORY

    override val equipLocation: ItemInstance.ItemLocation
        get() = ItemInstance.ItemLocation.PAPERDOLL

    override val adena: Int
        get() = if (adenaInstance != null) adenaInstance!!.count else 0

    val ancientAdena: Int
        get() = if (ancientAdenaInstance != null) ancientAdenaInstance!!.count else 0

    /**
     * @return a List of all sellable items.
     */
    val sellableItems: List<ItemInstance>
        get() = _items.filter { i -> !i.isEquipped && i.isSellable && (owner.pet == null || i.objectId != owner.pet!!.controlItemId) }.toList()

    /**
     * Get all augmented items
     * @return
     */
    val augmentedItems: Array<ItemInstance>
        get() {
            val list = ArrayList<ItemInstance>()
            for (item in _items) {
                if (item.isAugmented)
                    list.add(item)
            }
            return list.toTypedArray()
        }

    init {

        addPaperdollListener(ArmorSetListener)
        addPaperdollListener(BowRodListener)
        addPaperdollListener(ItemPassiveSkillsListener)
        addPaperdollListener(ShadowItemTaskManager)
    }

    /**
     * Returns the list of items in inventory available for transaction
     * @param allowAdena
     * @param allowAncientAdena
     * @param onlyAvailable
     * @return ItemInstance : items in inventory
     */
    @JvmOverloads
    fun getUniqueItems(
        allowAdena: Boolean,
        allowAncientAdena: Boolean,
        onlyAvailable: Boolean = true
    ): Array<ItemInstance> {
        val list = ArrayList<ItemInstance>()
        for (item in _items) {
            if (!allowAdena && item.itemId == ADENA_ID)
                continue

            if (!allowAncientAdena && item.itemId == ANCIENT_ADENA_ID)
                continue

            var isDuplicate = false
            for (litem in list) {
                if (litem.itemId == item.itemId) {
                    isDuplicate = true
                    break
                }
            }
            if (!isDuplicate && (!onlyAvailable || item.isSellable && item.isAvailable(owner, false, false)))
                list.add(item)
        }
        return list.toTypedArray()
    }

    @JvmOverloads
    fun getUniqueItemsByEnchantLevel(
        allowAdena: Boolean,
        allowAncientAdena: Boolean,
        onlyAvailable: Boolean = true
    ): Array<ItemInstance> {
        val list = ArrayList<ItemInstance>()
        for (item in _items) {
            if (!allowAdena && item.itemId == ADENA_ID)
                continue

            if (!allowAncientAdena && item.itemId == ANCIENT_ADENA_ID)
                continue

            var isDuplicate = false
            for (litem in list) {
                if (litem.itemId == item.itemId && litem.enchantLevel == item.enchantLevel) {
                    isDuplicate = true
                    break
                }
            }
            if (!isDuplicate && (!onlyAvailable || item.isSellable && item.isAvailable(owner, false, false)))
                list.add(item)
        }
        return list.toTypedArray()
    }

    /**
     * Returns the list of all items in inventory that have a given item id.
     * @param itemId : ID of item
     * @param includeEquipped : include equipped items
     * @return ItemInstance[] : matching items from inventory
     */
    @JvmOverloads
    fun getAllItemsByItemId(itemId: Int, includeEquipped: Boolean = true): Array<ItemInstance> {
        val list = ArrayList<ItemInstance>()
        for (item in _items) {
            if (item.itemId == itemId && (includeEquipped || !item.isEquipped))
                list.add(item)
        }
        return list.toTypedArray()
    }

    /**
     * Returns the list of all items in inventory that have a given item id AND a given enchantment level.
     * @param itemId : ID of item
     * @param enchantment : enchant level of item
     * @param includeEquipped : include equipped items
     * @return ItemInstance[] : matching items from inventory
     */
    @JvmOverloads
    fun getAllItemsByItemId(itemId: Int, enchantment: Int, includeEquipped: Boolean = true): Array<ItemInstance> {
        val list = ArrayList<ItemInstance>()
        for (item in _items) {
            if (item.itemId == itemId && item.enchantLevel == enchantment && (includeEquipped || !item.isEquipped))
                list.add(item)
        }
        return list.toTypedArray()
    }

    /**
     * Returns the list of items in inventory available for transaction
     * @param allowAdena
     * @param allowNonTradeable
     * @return ItemInstance : items in inventory
     */
    fun getAvailableItems(allowAdena: Boolean, allowNonTradeable: Boolean): Array<ItemInstance> {
        val list = ArrayList<ItemInstance>()
        for (item in _items) {
            if (item.isAvailable(owner, allowAdena, allowNonTradeable))
                list.add(item)
        }
        return list.toTypedArray()
    }

    /**
     * Returns the list of items in inventory available for transaction adjusetd by tradeList
     * @param tradeList
     * @return ItemInstance : items in inventory
     */
    fun getAvailableItems(tradeList: TradeList): Array<TradeItem> {
        val list = ArrayList<TradeItem>()
        for (item in _items) {
            if (item != null && item.isAvailable(owner, false, false)) {
                val adjItem = tradeList.adjustAvailableItem(item)
                if (adjItem != null)
                    list.add(adjItem)
            }
        }
        return list.toTypedArray()
    }

    /**
     * Adjust TradeItem according his status in inventory
     * @param item : ItemInstance to be adjusten
     */
    fun adjustAvailableItem(item: TradeItem) {
        var notAllEquipped = false
        for (adjItem in getItemsByItemId(item.item.itemId)) {
            if (adjItem.isEquipable) {
                if (!adjItem.isEquipped)
                    notAllEquipped = notAllEquipped or true
            } else {
                notAllEquipped = notAllEquipped or true
                break
            }
        }
        if (notAllEquipped) {
            val adjItem = getItemByItemId(item.item.itemId)
            item.objectId = adjItem!!.objectId
            item.enchant = adjItem.enchantLevel

            if (adjItem.count < item.count)
                item.count = adjItem.count

            return
        }

        item.count = 0
    }

    /**
     * Adds adena to PCInventory
     * @param process : String Identifier of process triggering this action
     * @param count : int Quantity of adena to be added
     * @param actor : Player Player requesting the item add
     * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
     */
    fun addAdena(process: String, count: Int, actor: Player, reference: WorldObject) {
        if (count > 0)
            addItem(process, ADENA_ID, count, actor, reference)
    }

    /**
     * Removes adena to PCInventory
     * @param process : String Identifier of process triggering this action
     * @param count : int Quantity of adena to be removed
     * @param actor : Player Player requesting the item add
     * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @return true if successful.
     */
    fun reduceAdena(process: String, count: Int, actor: Player, reference: WorldObject): Boolean {
        return if (count > 0) destroyItemByItemId(process, ADENA_ID, count, actor, reference) != null else false

    }

    /**
     * Adds specified amount of ancient adena to player inventory.
     * @param process : String Identifier of process triggering this action
     * @param count : int Quantity of adena to be added
     * @param actor : Player Player requesting the item add
     * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
     */
    fun addAncientAdena(process: String, count: Int, actor: Player, reference: WorldObject) {
        if (count > 0)
            addItem(process, ANCIENT_ADENA_ID, count, actor, reference)
    }

    /**
     * Removes specified amount of ancient adena from player inventory.
     * @param process : String Identifier of process triggering this action
     * @param count : int Quantity of adena to be removed
     * @param actor : Player Player requesting the item add
     * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @return true if successful.
     */
    fun reduceAncientAdena(process: String, count: Int, actor: Player, reference: WorldObject): Boolean {
        return if (count > 0) destroyItemByItemId(process, ANCIENT_ADENA_ID, count, actor, reference) != null else false

    }

    /**
     * Adds item in inventory and checks _adena and _ancientAdena
     * @param process : String Identifier of process triggering this action
     * @param item : ItemInstance to be added
     * @param actor : Player Player requesting the item add
     * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @return ItemInstance corresponding to the new item or the updated item in inventory
     */
    override fun addItem(process: String, item: ItemInstance, actor: Player?, reference: WorldObject?): ItemInstance? {
        var item = item
        item = super.addItem(process, item, actor, reference) ?: return null

        if (item.itemId == ADENA_ID && item != adenaInstance)
            adenaInstance = item
        else if (item.itemId == ANCIENT_ADENA_ID && item != ancientAdenaInstance)
            ancientAdenaInstance = item

        return item
    }

    /**
     * Adds item in inventory and checks _adena and _ancientAdena
     * @param process : String Identifier of process triggering this action
     * @param itemId : int Item Identifier of the item to be added
     * @param count : int Quantity of items to be added
     * @param actor : Player Player requesting the item creation
     * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @return ItemInstance corresponding to the new item or the updated item in inventory
     */
    override fun addItem(
        process: String,
        itemId: Int,
        count: Int,
        actor: Player?,
        reference: WorldObject?
    ): ItemInstance? {
        val item = super.addItem(process, itemId, count, actor, reference) ?: return null

        if (item.itemId == ADENA_ID && item != adenaInstance)
            adenaInstance = item
        else if (item.itemId == ANCIENT_ADENA_ID && item != ancientAdenaInstance)
            ancientAdenaInstance = item

        if (actor != null) {
            // Send inventory update packet
            val playerIU = InventoryUpdate()
            playerIU.addItem(item)
            actor.sendPacket(playerIU)

            // Update current load as well
            val su = StatusUpdate(actor)
            su.addAttribute(StatusUpdate.CUR_LOAD, actor.currentLoad)
            actor.sendPacket(su)
        }

        return item
    }

    /**
     * Transfers item to another inventory and checks _adena and _ancientAdena
     * @param process : String Identifier of process triggering this action
     * @param objectId : int Item Identifier of the item to be transfered
     * @param count : int Quantity of items to be transfered
     * @param actor : Player Player requesting the item transfer
     * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @return ItemInstance corresponding to the new item or the updated item in inventory
     */
    override fun transferItem(
        process: String,
        objectId: Int,
        count: Int,
        target: ItemContainer?,
        actor: Player?,
        reference: WorldObject?
    ): ItemInstance? {
        val item = super.transferItem(process, objectId, count, target, actor, reference)

        if (adenaInstance != null && (adenaInstance!!.count <= 0 || adenaInstance!!.ownerId != ownerId))
            adenaInstance = null

        if (ancientAdenaInstance != null && (ancientAdenaInstance!!.count <= 0 || ancientAdenaInstance!!.ownerId != ownerId))
            ancientAdenaInstance = null

        return item
    }

    /**
     * Destroy item from inventory and checks _adena and _ancientAdena
     * @param process : String Identifier of process triggering this action
     * @param item : ItemInstance to be destroyed
     * @param actor : Player Player requesting the item destroy
     * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @return ItemInstance corresponding to the destroyed item or the updated item in inventory
     */
    override fun destroyItem(
        process: String,
        item: ItemInstance,
        actor: Player?,
        reference: WorldObject?
    ): ItemInstance? {
        return destroyItem(process, item, item.count, actor, reference)
    }

    /**
     * Destroy item from inventory and checks _adena and _ancientAdena
     * @param process : String Identifier of process triggering this action
     * @param item : ItemInstance to be destroyed
     * @param actor : Player Player requesting the item destroy
     * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @return ItemInstance corresponding to the destroyed item or the updated item in inventory
     */
    override fun destroyItem(
        process: String?,
        item: ItemInstance,
        count: Int,
        actor: Player?,
        reference: WorldObject?
    ): ItemInstance? {
        var item = item
        item = super.destroyItem(process, item, count, actor, reference) ?: return null

        if (adenaInstance != null && adenaInstance!!.count <= 0)
            adenaInstance = null

        if (ancientAdenaInstance != null && ancientAdenaInstance!!.count <= 0)
            ancientAdenaInstance = null

        return item
    }

    /**
     * Destroys item from inventory and checks _adena and _ancientAdena
     * @param process : String Identifier of process triggering this action
     * @param objectId : int Item Instance identifier of the item to be destroyed
     * @param count : int Quantity of items to be destroyed
     * @param actor : Player Player requesting the item destroy
     * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @return ItemInstance corresponding to the destroyed item or the updated item in inventory
     */
    override fun destroyItem(
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
     * Destroy item from inventory by using its <B>itemId</B> and checks _adena and _ancientAdena
     * @param process : String Identifier of process triggering this action
     * @param itemId : int Item identifier of the item to be destroyed
     * @param count : int Quantity of items to be destroyed
     * @param actor : Player Player requesting the item destroy
     * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @return ItemInstance corresponding to the destroyed item or the updated item in inventory
     */
    override fun destroyItemByItemId(
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
     * Drop item from inventory and checks _adena and _ancientAdena
     * @param process : String Identifier of process triggering this action
     * @param item : ItemInstance to be dropped
     * @param actor : Player Player requesting the item drop
     * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @return ItemInstance corresponding to the destroyed item or the updated item in inventory
     */
    override fun dropItem(process: String, item: ItemInstance?, actor: Player, reference: WorldObject): ItemInstance? {
        var item = item
        item = super.dropItem(process, item, actor, reference)

        if (adenaInstance != null && (adenaInstance!!.count <= 0 || adenaInstance!!.ownerId != ownerId))
            adenaInstance = null

        if (ancientAdenaInstance != null && (ancientAdenaInstance!!.count <= 0 || ancientAdenaInstance!!.ownerId != ownerId))
            ancientAdenaInstance = null

        return item
    }

    /**
     * Drop item from inventory by using its <B>objectID</B> and checks _adena and _ancientAdena
     * @param process : String Identifier of process triggering this action
     * @param objectId : int Item Instance identifier of the item to be dropped
     * @param count : int Quantity of items to be dropped
     * @param actor : Player Player requesting the item drop
     * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @return ItemInstance corresponding to the destroyed item or the updated item in inventory
     */
    override fun dropItem(
        process: String,
        objectId: Int,
        count: Int,
        actor: Player,
        reference: WorldObject
    ): ItemInstance? {
        val item = super.dropItem(process, objectId, count, actor, reference)

        if (adenaInstance != null && (adenaInstance!!.count <= 0 || adenaInstance!!.ownerId != ownerId))
            adenaInstance = null

        if (ancientAdenaInstance != null && (ancientAdenaInstance!!.count <= 0 || ancientAdenaInstance!!.ownerId != ownerId))
            ancientAdenaInstance = null

        return item
    }

    /**
     * **Overloaded**, when removes item from inventory, remove also owner shortcuts.
     * @param item : ItemInstance to be removed from inventory
     */
    override fun removeItem(item: ItemInstance): Boolean {
        // Removes any reference to the item from Shortcut bar
        owner.removeItemFromShortCut(item.objectId)

        // Removes active Enchant Scroll
        if (item == owner.activeEnchantItem)
            owner.activeEnchantItem = null

        if (item.itemId == ADENA_ID)
            adenaInstance = null
        else if (item.itemId == ANCIENT_ADENA_ID)
            ancientAdenaInstance = null

        return super.removeItem(item)
    }

    /**
     * Refresh the weight of equipment loaded
     */
    public override fun refreshWeight() {
        super.refreshWeight()

        owner.refreshOverloaded()
    }

    /**
     * Get back items in inventory from database
     */
    override fun restore() {
        super.restore()

        adenaInstance = getItemByItemId(ADENA_ID)
        ancientAdenaInstance = getItemByItemId(ANCIENT_ADENA_ID)
    }

    fun validateCapacity(item: ItemInstance): Boolean {
        var slots = 0

        if (!(item.isStackable && getItemByItemId(item.itemId) != null) && item.itemType !== EtcItemType.HERB)
            slots++

        return validateCapacity(slots)
    }

    fun validateCapacityByItemId(ItemId: Int): Boolean {
        var slots = 0

        val invItem = getItemByItemId(ItemId)
        if (!(invItem != null && invItem.isStackable))
            slots++

        return validateCapacity(slots)
    }

    override fun validateCapacity(slots: Int): Boolean {
        return _items.size + slots <= owner.inventoryLimit
    }

    override fun validateWeight(weight: Int): Boolean {
        return totalWeight + weight <= owner.maxLoad
    }

    override fun toString(): String {
        return javaClass.simpleName + "[" + owner + "]"
    }

    companion object {
        val ADENA_ID = 57
        val ANCIENT_ADENA_ID = 5575
    }
}
/**
 * Returns the list of items in inventory available for transaction Allows an item to appear twice if and only if there is a difference in enchantment level.
 * @param allowAdena
 * @param allowAncientAdena
 * @return ItemInstance : items in inventory
 */
/**
 * @param itemId
 * @return
 * @see PcInventory.getAllItemsByItemId
 */
/**
 * @param itemId
 * @param enchantment
 * @return
 * @see PcInventory.getAllItemsByItemId
 */