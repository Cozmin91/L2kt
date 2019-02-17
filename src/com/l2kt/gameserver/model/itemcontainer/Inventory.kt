package com.l2kt.gameserver.model.itemcontainer

import com.l2kt.L2DatabaseFactory
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.entity.Hero
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.item.kind.Item
import com.l2kt.gameserver.model.item.type.ArmorType
import com.l2kt.gameserver.model.item.type.CrystalType
import com.l2kt.gameserver.model.item.type.EtcItemType
import com.l2kt.gameserver.model.item.type.WeaponType
import com.l2kt.gameserver.model.itemcontainer.listeners.OnEquipListener
import com.l2kt.gameserver.model.itemcontainer.listeners.StatsListener
import java.util.*

/**
 * This class manages inventory
 */
abstract class Inventory
/**
 * Constructor of the inventory
 */
protected constructor() : ItemContainer() {

    private val _paperdoll: Array<ItemInstance?>
    private val _paperdollListeners: MutableList<OnEquipListener>

    // protected to be accessed from child classes only
    /**
     * Returns the totalWeight.
     * @return int
     */
    var totalWeight: Int = 0
        protected set

    // used to quickly check for using of items of special type
    /**
     * Return the mask of worn item
     * @return int
     */
    var wornMask: Int = 0
        private set

    protected abstract val equipLocation: ItemInstance.ItemLocation

    /**
     * @return The list of worn ItemInstance items.
     */
    val paperdollItems: List<ItemInstance>
        get() {
            val itemsList = ArrayList<ItemInstance>()

            for (item in _paperdoll) {
                if (item != null)
                    itemsList.add(item)
            }
            return itemsList
        }

    // Recorder of alterations in inventory
    class ChangeRecorder
    /**
     * Constructor of the ChangeRecorder
     * @param inventory
     */
    internal constructor(private val _inventory: Inventory) : OnEquipListener {
        private val _changed: MutableList<ItemInstance>

        /**
         * Returns alterations in inventory
         * @return ItemInstance[] : array of alterated items
         */
        val changedItems: Array<ItemInstance>
            get() = _changed.toTypedArray()

        init {
            _changed = ArrayList()
            _inventory.addPaperdollListener(this)
        }

        /**
         * Add alteration in inventory when item equipped
         */
        override fun onEquip(slot: Int, item: ItemInstance, actor: Playable) {
            if (!_changed.contains(item))
                _changed.add(item)
        }

        /**
         * Add alteration in inventory when item unequipped
         */
        override fun onUnequip(slot: Int, item: ItemInstance, actor: Playable) {
            if (!_changed.contains(item))
                _changed.add(item)
        }
    }

    init {
        _paperdoll = arrayOfNulls(PAPERDOLL_TOTALSLOTS)
        _paperdollListeners = ArrayList()

        // common
        addPaperdollListener(StatsListener)
    }

    /**
     * Returns the instance of new ChangeRecorder
     * @return ChangeRecorder
     */
    fun newRecorder(): ChangeRecorder {
        return ChangeRecorder(this)
    }

    /**
     * Drop item from inventory and updates database
     * @param process : String Identifier of process triggering this action
     * @param item : ItemInstance to be dropped
     * @param actor : Player Player requesting the item drop
     * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @return ItemInstance corresponding to the destroyed item or the updated item in inventory
     */
    open fun dropItem(process: String, item: ItemInstance?, actor: Player?, reference: WorldObject?): ItemInstance? {
        if (item == null)
            return null

        synchronized(item) {
            if (!_items.contains(item))
                return null

            removeItem(item)
            item.setOwnerId(process, 0, actor, reference)
            item.location = ItemInstance.ItemLocation.VOID
            item.lastChange = ItemInstance.ItemState.REMOVED

            item.updateDatabase()
            refreshWeight()
        }
        return item
    }

    /**
     * Drop item from inventory by using its <B>objectID</B> and updates database
     * @param process : String Identifier of process triggering this action
     * @param objectId : int Item Instance identifier of the item to be dropped
     * @param count : int Quantity of items to be dropped
     * @param actor : Player Player requesting the item drop
     * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @return ItemInstance corresponding to the destroyed item or the updated item in inventory
     */
    open fun dropItem(
        process: String,
        objectId: Int,
        count: Int,
        actor: Player?,
        reference: WorldObject?
    ): ItemInstance? {
        var item: ItemInstance? = getItemByObjectId(objectId) ?: return null

        if(item != null)
            synchronized(item) {
                if (!_items.contains(item!!))
                    return null

                // Adjust item quantity and create new instance to drop
                // Directly drop entire item
                if (item!!.count > count) {
                    item!!.changeCount(process, -count, actor, reference)
                    item!!.lastChange = ItemInstance.ItemState.MODIFIED
                    item!!.updateDatabase()

                    item = ItemInstance.create(item!!.itemId, count, actor, reference)
                    item!!.updateDatabase()
                    refreshWeight()
                    return item
                }
            }
        return dropItem(process, item, actor, reference)
    }

    /**
     * Adds item to inventory for further adjustments and Equip it if necessary (itemlocation defined)<BR></BR>
     * <BR></BR>
     * @param item : ItemInstance to be added from inventory
     */
    override fun addItem(item: ItemInstance) {
        super.addItem(item)
        if (item.isEquipped)
            equipItem(item)
    }

    /**
     * Removes item from inventory for further adjustments.
     * @param item : ItemInstance to be removed from inventory
     */
    override fun removeItem(item: ItemInstance): Boolean {
        // Unequip item if equipped
        for (i in _paperdoll.indices) {
            if (_paperdoll[i] == item)
                unEquipItemInSlot(i)
        }
        return super.removeItem(item)
    }

    /**
     * @param slot The slot to check.
     * @return The ItemInstance item in the paperdoll slot.
     */
    fun getPaperdollItem(slot: Int): ItemInstance? {
        return _paperdoll[slot]
    }

    /**
     * @param slot Item slot identifier
     * @return the ItemInstance item in the paperdoll Item slot
     */
    fun getPaperdollItemByL2ItemId(slot: Int): ItemInstance? {
        val index = getPaperdollIndex(slot)
        return if (index == -1) null else _paperdoll[index]

    }

    /**
     * Returns the ID of the item in the paperdol slot
     * @param slot : int designating the slot
     * @return int designating the ID of the item
     */
    fun getPaperdollItemId(slot: Int): Int {
        val item = _paperdoll[slot]
        return item?.itemId ?: 0

    }

    fun getPaperdollAugmentationId(slot: Int): Int {
        val item = _paperdoll[slot]
        if (item != null) {
            if (item.getAugmentation() != null)
                return item.getAugmentation()!!.getAugmentationId()
        }
        return 0
    }

    /**
     * Returns the objectID associated to the item in the paperdoll slot
     * @param slot : int pointing out the slot
     * @return int designating the objectID
     */
    fun getPaperdollObjectId(slot: Int): Int {
        val item = _paperdoll[slot]
        return item?.objectId ?: 0

    }

    /**
     * Adds new inventory's paperdoll listener
     * @param listener PaperdollListener pointing out the listener
     */
    @Synchronized
    fun addPaperdollListener(listener: OnEquipListener) {
        assert(!_paperdollListeners.contains(listener))
        _paperdollListeners.add(listener)
    }

    /**
     * Removes a paperdoll listener
     * @param listener PaperdollListener pointing out the listener to be deleted
     */
    @Synchronized
    fun removePaperdollListener(listener: OnEquipListener) {
        _paperdollListeners.remove(listener)
    }

    /**
     * Equips an item in the given slot of the paperdoll. <U><I>Remark :</I></U> The item <B>HAS TO BE</B> already in the inventory
     * @param slot : int pointing out the slot of the paperdoll
     * @param item : ItemInstance pointing out the item to add in slot
     * @return ItemInstance designating the item placed in the slot before
     */
    @Synchronized
    fun setPaperdollItem(slot: Int, item: ItemInstance?): ItemInstance? {
        val old = _paperdoll[slot]
        if (old != item) {
            if (old != null) {
                _paperdoll[slot] = null
                // Put old item from paperdoll slot to base location
                old.location = baseLocation
                old.lastChange = ItemInstance.ItemState.MODIFIED

                // delete armor mask flag (in case of two-piece armor it does not matter, we need to deactivate mask too)
                wornMask = wornMask and old.item.itemMask.inv()

                // Notify all paperdoll listener in order to unequip old item in slot
                for (listener in _paperdollListeners) {
                    if (listener == null)
                        continue

                    listener.onUnequip(slot, old, (owner as Playable?)!!)
                }
                old.updateDatabase()
            }
            // Add new item in slot of paperdoll
            if (item != null) {
                _paperdoll[slot] = item
                item.setLocation(equipLocation, slot)
                item.lastChange = ItemInstance.ItemState.MODIFIED

                // activate mask (check 2nd armor part for two-piece armors)
                val armor = item.item
                if (armor.bodyPart == Item.SLOT_CHEST) {
                    val legs = _paperdoll[PAPERDOLL_LEGS]
                    if (legs != null && legs.item.itemMask == armor.itemMask)
                        wornMask = wornMask or armor.itemMask
                } else if (armor.bodyPart == Item.SLOT_LEGS) {
                    val legs = _paperdoll[PAPERDOLL_CHEST]
                    if (legs != null && legs.item.itemMask == armor.itemMask)
                        wornMask = wornMask or armor.itemMask
                } else
                    wornMask = wornMask or armor.itemMask

                for (listener in _paperdollListeners) {
                    if (listener == null)
                        continue

                    listener.onEquip(slot, item, (owner as Playable?)!!)
                }
                item.updateDatabase()
            }
        }
        return old
    }

    fun getSlotFromItem(item: ItemInstance): Int {
        var slot = -1
        val location = item.locationSlot

        when (location) {
            PAPERDOLL_UNDER -> slot = Item.SLOT_UNDERWEAR
            PAPERDOLL_LEAR -> slot = Item.SLOT_L_EAR
            PAPERDOLL_REAR -> slot = Item.SLOT_R_EAR
            PAPERDOLL_NECK -> slot = Item.SLOT_NECK
            PAPERDOLL_RFINGER -> slot = Item.SLOT_R_FINGER
            PAPERDOLL_LFINGER -> slot = Item.SLOT_L_FINGER
            PAPERDOLL_HAIR -> slot = Item.SLOT_HAIR
            PAPERDOLL_FACE -> slot = Item.SLOT_FACE
            PAPERDOLL_HEAD -> slot = Item.SLOT_HEAD
            PAPERDOLL_RHAND -> slot = Item.SLOT_R_HAND
            PAPERDOLL_LHAND -> slot = Item.SLOT_L_HAND
            PAPERDOLL_GLOVES -> slot = Item.SLOT_GLOVES
            PAPERDOLL_CHEST -> slot = item.item.bodyPart
            PAPERDOLL_LEGS -> slot = Item.SLOT_LEGS
            PAPERDOLL_BACK -> slot = Item.SLOT_BACK
            PAPERDOLL_FEET -> slot = Item.SLOT_FEET
        }// fall through

        return slot
    }

    /**
     * Unequips item in body slot and returns alterations.
     * @param item : the item used to find the slot back.
     * @return ItemInstance[] : list of changes
     */
    fun unEquipItemInBodySlotAndRecord(item: ItemInstance): Array<ItemInstance> {
        val recorder = newRecorder()

        try {
            unEquipItemInBodySlot(getSlotFromItem(item))
        } finally {
            removePaperdollListener(recorder)
        }
        return recorder.changedItems
    }

    /**
     * Unequips item in body slot and returns alterations.
     * @param slot : int designating the slot of the paperdoll
     * @return ItemInstance[] : list of changes
     */
    fun unEquipItemInBodySlotAndRecord(slot: Int): Array<ItemInstance> {
        val recorder = newRecorder()

        try {
            unEquipItemInBodySlot(slot)
        } finally {
            removePaperdollListener(recorder)
        }
        return recorder.changedItems
    }

    /**
     * Sets item in slot of the paperdoll to null value
     * @param pdollSlot : int designating the slot
     * @return ItemInstance designating the item in slot before change
     */
    fun unEquipItemInSlot(pdollSlot: Int): ItemInstance? {
        return setPaperdollItem(pdollSlot, null)
    }

    /**
     * Unepquips item in slot and returns alterations
     * @param slot : int designating the slot
     * @return ItemInstance[] : list of items altered
     */
    fun unEquipItemInSlotAndRecord(slot: Int): Array<ItemInstance> {
        val recorder = newRecorder()

        try {
            unEquipItemInSlot(slot)
            if (owner is Player)
                (owner as Player).refreshExpertisePenalty()
        } finally {
            removePaperdollListener(recorder)
        }
        return recorder.changedItems
    }

    /**
     * Unequips item in slot (i.e. equips with default value)
     * @param slot : int designating the slot
     * @return the instance of the item.
     */
    fun unEquipItemInBodySlot(slot: Int): ItemInstance? {
        var pdollSlot = -1

        when (slot) {
            Item.SLOT_L_EAR -> pdollSlot = PAPERDOLL_LEAR
            Item.SLOT_R_EAR -> pdollSlot = PAPERDOLL_REAR
            Item.SLOT_NECK -> pdollSlot = PAPERDOLL_NECK
            Item.SLOT_R_FINGER -> pdollSlot = PAPERDOLL_RFINGER
            Item.SLOT_L_FINGER -> pdollSlot = PAPERDOLL_LFINGER
            Item.SLOT_HAIR -> pdollSlot = PAPERDOLL_HAIR
            Item.SLOT_FACE -> pdollSlot = PAPERDOLL_FACE
            Item.SLOT_HAIRALL -> {
                setPaperdollItem(PAPERDOLL_FACE, null)
                pdollSlot = PAPERDOLL_FACE
            }
            Item.SLOT_HEAD -> pdollSlot = PAPERDOLL_HEAD
            Item.SLOT_R_HAND, Item.SLOT_LR_HAND -> pdollSlot = PAPERDOLL_RHAND
            Item.SLOT_L_HAND -> pdollSlot = PAPERDOLL_LHAND
            Item.SLOT_GLOVES -> pdollSlot = PAPERDOLL_GLOVES
            Item.SLOT_CHEST, Item.SLOT_FULL_ARMOR, Item.SLOT_ALLDRESS -> pdollSlot = PAPERDOLL_CHEST
            Item.SLOT_LEGS -> pdollSlot = PAPERDOLL_LEGS
            Item.SLOT_BACK -> pdollSlot = PAPERDOLL_BACK
            Item.SLOT_FEET -> pdollSlot = PAPERDOLL_FEET
            Item.SLOT_UNDERWEAR -> pdollSlot = PAPERDOLL_UNDER
            else -> LOGGER.warn("Slot type {} is unhandled.", slot)
        }

        if (pdollSlot >= 0) {
            val old = setPaperdollItem(pdollSlot, null)
            if (old != null) {
                if (owner is Player)
                    (owner as Player).refreshExpertisePenalty()
            }
            return old
        }
        return null
    }

    /**
     * Equips item and returns list of alterations<BR></BR>
     * <B>If you dont need return value use [Inventory.equipItem] instead</B>
     * @param item : ItemInstance corresponding to the item
     * @return ItemInstance[] : list of alterations
     */
    fun equipItemAndRecord(item: ItemInstance): Array<ItemInstance> {
        val recorder = newRecorder()

        try {
            equipItem(item)
        } finally {
            removePaperdollListener(recorder)
        }
        return recorder.changedItems
    }

    /**
     * Equips item in slot of paperdoll.
     * @param item : ItemInstance designating the item and slot used.
     */
    fun equipItem(item: ItemInstance) {
        if (owner is Player) {
            // Can't equip item if you are in shop mod or hero item and you're not hero.
            if ((owner as Player).isInStoreMode || item.isHeroItem && !Hero.isActiveHero(ownerId))
                return
        }

        val targetSlot = item.item.bodyPart

        // check if player wear formal
        val formal = getPaperdollItem(PAPERDOLL_CHEST)
        if (formal != null && formal.item.bodyPart == Item.SLOT_ALLDRESS) {
            // only chest target can pass this
            when (targetSlot) {
                Item.SLOT_LR_HAND, Item.SLOT_L_HAND, Item.SLOT_R_HAND -> unEquipItemInBodySlotAndRecord(Item.SLOT_ALLDRESS)
                Item.SLOT_LEGS, Item.SLOT_FEET, Item.SLOT_GLOVES, Item.SLOT_HEAD -> return
            }
        }

        when (targetSlot) {
            Item.SLOT_LR_HAND -> {
                setPaperdollItem(PAPERDOLL_LHAND, null)
                setPaperdollItem(PAPERDOLL_RHAND, item)
            }

            Item.SLOT_L_HAND -> {
                val rh = getPaperdollItem(PAPERDOLL_RHAND)
                if (rh != null && rh.item.bodyPart == Item.SLOT_LR_HAND && !(rh.itemType === WeaponType.BOW && item.itemType === EtcItemType.ARROW || rh.itemType === WeaponType.FISHINGROD && item.itemType === EtcItemType.LURE))
                    setPaperdollItem(PAPERDOLL_RHAND, null)

                setPaperdollItem(PAPERDOLL_LHAND, item)
            }

            Item.SLOT_R_HAND ->
                // dont care about arrows, listener will unequip them (hopefully)
                setPaperdollItem(PAPERDOLL_RHAND, item)

            Item.SLOT_L_EAR, Item.SLOT_R_EAR, Item.SLOT_L_EAR or Item.SLOT_R_EAR -> if (_paperdoll[PAPERDOLL_LEAR] == null)
                setPaperdollItem(PAPERDOLL_LEAR, item)
            else if (_paperdoll[PAPERDOLL_REAR] == null)
                setPaperdollItem(PAPERDOLL_REAR, item)
            else {
                if (_paperdoll[PAPERDOLL_REAR]?.itemId == item.itemId)
                    setPaperdollItem(PAPERDOLL_LEAR, item)
                else if (_paperdoll[PAPERDOLL_LEAR]?.itemId == item.itemId)
                    setPaperdollItem(PAPERDOLL_REAR, item)
                else
                    setPaperdollItem(PAPERDOLL_LEAR, item)
            }

            Item.SLOT_L_FINGER, Item.SLOT_R_FINGER, Item.SLOT_L_FINGER or Item.SLOT_R_FINGER -> if (_paperdoll[PAPERDOLL_LFINGER] == null)
                setPaperdollItem(PAPERDOLL_LFINGER, item)
            else if (_paperdoll[PAPERDOLL_RFINGER] == null)
                setPaperdollItem(PAPERDOLL_RFINGER, item)
            else {
                if (_paperdoll[PAPERDOLL_RFINGER]?.itemId == item.itemId)
                    setPaperdollItem(PAPERDOLL_LFINGER, item)
                else if (_paperdoll[PAPERDOLL_LFINGER]?.itemId == item.itemId)
                    setPaperdollItem(PAPERDOLL_RFINGER, item)
                else
                    setPaperdollItem(PAPERDOLL_LFINGER, item)
            }

            Item.SLOT_NECK -> setPaperdollItem(PAPERDOLL_NECK, item)

            Item.SLOT_FULL_ARMOR -> {
                setPaperdollItem(PAPERDOLL_LEGS, null)
                setPaperdollItem(PAPERDOLL_CHEST, item)
            }

            Item.SLOT_CHEST -> setPaperdollItem(PAPERDOLL_CHEST, item)

            Item.SLOT_LEGS -> {
                // handle full armor
                val chest = getPaperdollItem(PAPERDOLL_CHEST)
                if (chest != null && chest.item.bodyPart == Item.SLOT_FULL_ARMOR)
                    setPaperdollItem(PAPERDOLL_CHEST, null)

                setPaperdollItem(PAPERDOLL_LEGS, item)
            }

            Item.SLOT_FEET -> setPaperdollItem(PAPERDOLL_FEET, item)

            Item.SLOT_GLOVES -> setPaperdollItem(PAPERDOLL_GLOVES, item)

            Item.SLOT_HEAD -> setPaperdollItem(PAPERDOLL_HEAD, item)

            Item.SLOT_FACE -> {
                val hair = getPaperdollItem(PAPERDOLL_HAIR)
                if (hair != null && hair.item.bodyPart == Item.SLOT_HAIRALL)
                    setPaperdollItem(PAPERDOLL_HAIR, null)

                setPaperdollItem(PAPERDOLL_FACE, item)
            }

            Item.SLOT_HAIR -> {
                val face = getPaperdollItem(PAPERDOLL_FACE)
                if (face != null && face.item.bodyPart == Item.SLOT_HAIRALL)
                    setPaperdollItem(PAPERDOLL_FACE, null)

                setPaperdollItem(PAPERDOLL_HAIR, item)
            }

            Item.SLOT_HAIRALL -> {
                setPaperdollItem(PAPERDOLL_FACE, null)
                setPaperdollItem(PAPERDOLL_HAIR, item)
            }

            Item.SLOT_UNDERWEAR -> setPaperdollItem(PAPERDOLL_UNDER, item)

            Item.SLOT_BACK -> setPaperdollItem(PAPERDOLL_BACK, item)

            Item.SLOT_ALLDRESS -> {
                // formal dress
                setPaperdollItem(PAPERDOLL_LEGS, null)
                setPaperdollItem(PAPERDOLL_LHAND, null)
                setPaperdollItem(PAPERDOLL_RHAND, null)
                setPaperdollItem(PAPERDOLL_HEAD, null)
                setPaperdollItem(PAPERDOLL_FEET, null)
                setPaperdollItem(PAPERDOLL_GLOVES, null)
                setPaperdollItem(PAPERDOLL_CHEST, item)
            }

            else -> ItemContainer.Companion.LOGGER.warn("Unknown body slot {} for itemId {}.", targetSlot, item.itemId)
        }
    }

    /**
     * Equips pet item in slot of paperdoll. Concerning pets, armors go to chest location, and weapon to R-hand.
     * @param item : ItemInstance designating the item and slot used.
     */
    fun equipPetItem(item: ItemInstance) {
        if (owner is Player) {
            // Can't equip item if you are in shop mod.
            if ((owner as Player).isInStoreMode)
                return
        }

        // Verify first if item is a pet item.
        if (item.isPetItem) {
            // Check then about type of item : armor or weapon. Feed the correct slot.
            if (item.itemType === WeaponType.PET)
                setPaperdollItem(PAPERDOLL_RHAND, item)
            else if (item.itemType === ArmorType.PET)
                setPaperdollItem(PAPERDOLL_CHEST, item)
        }
    }

    /**
     * Refresh the weight of equipment loaded
     */
    override fun refreshWeight() {
        var weight = 0

        for (item in _items) {
            if (item != null && item.item != null)
                weight += item.item.weight * item.count
        }

        totalWeight = weight
    }

    /**
     * Return the ItemInstance of the arrows needed for this bow.<BR></BR>
     * <BR></BR>
     * @param bow : L2Item designating the bow
     * @return ItemInstance pointing out arrows for bow
     */
    fun findArrowForBow(bow: Item?): ItemInstance? {
        if (bow == null)
            return null

        var arrowsId = 0

        when (bow.crystalType) {
            CrystalType.NONE -> arrowsId = 17
            CrystalType.D -> arrowsId = 1341
            CrystalType.C -> arrowsId = 1342
            CrystalType.B -> arrowsId = 1343
            CrystalType.A -> arrowsId = 1344
            CrystalType.S -> arrowsId = 1345
            else -> arrowsId = 17
        }// Wooden arrow
        // Bone arrow
        // Fine steel arrow
        // Silver arrow
        // Mithril arrow
        // Shining arrow

        // Get the ItemInstance corresponding to the item identifier and return it
        return getItemByItemId(arrowsId)
    }

    /**
     * Get back items in inventory from database
     */
    override fun restore() {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(RESTORE_INVENTORY).use { ps ->
                    ps.setInt(1, ownerId)
                    ps.setString(2, baseLocation.name)
                    ps.setString(3, equipLocation.name)

                    ps.executeQuery().use { rs ->
                        while (rs.next()) {
                            // Restore the item.
                            val item = ItemInstance.restoreFromDb(ownerId, rs) ?: continue

                            // If the item is an hero item and inventory's owner is a player who isn't an hero, then set it to inventory.
                            if (owner is Player && item.isHeroItem && !Hero.isActiveHero(ownerId))
                                item.location = ItemInstance.ItemLocation.INVENTORY

                            // Add the item to world objects list.
                            World.addObject(item)

                            // If stackable item is found in inventory just add to current quantity
                            if (item.isStackable && getItemByItemId(item.itemId) != null)
                                addItem("Restore", item, owner!!.actingPlayer, null)
                            else
                                addItem(item)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            ItemContainer.LOGGER.error("Couldn't restore inventory for {}.", e, ownerId)
        }

        refreshWeight()
    }

    /**
     * Re-notify to paperdoll listeners every equipped item
     */
    fun reloadEquippedItems() {
        for (element in _paperdoll) {
            if (element == null)
                continue

            val slot = element.locationSlot

            for (listener in _paperdollListeners) {
                listener.onUnequip(slot, element, (owner as Playable?)!!)
                listener.onEquip(slot, element, (owner as Playable?)!!)
            }
        }
    }

    companion object {
        const val PAPERDOLL_UNDER = 0
        const val PAPERDOLL_LEAR = 1
        const val PAPERDOLL_REAR = 2
        const val PAPERDOLL_NECK = 3
        const val PAPERDOLL_LFINGER = 4
        const val PAPERDOLL_RFINGER = 5
        const val PAPERDOLL_HEAD = 6
        const val PAPERDOLL_RHAND = 7
        const val PAPERDOLL_LHAND = 8
        const val PAPERDOLL_GLOVES = 9
        const val PAPERDOLL_CHEST = 10
        const val PAPERDOLL_LEGS = 11
        const val PAPERDOLL_FEET = 12
        const val PAPERDOLL_BACK = 13
        const val PAPERDOLL_FACE = 14
        const val PAPERDOLL_HAIR = 15
        const val PAPERDOLL_HAIRALL = 16
        const val PAPERDOLL_TOTALSLOTS = 17

        private const val RESTORE_INVENTORY =
            "SELECT object_id, item_id, count, enchant_level, loc, loc_data, custom_type1, custom_type2, mana_left, time FROM items WHERE owner_id=? AND (loc=? OR loc=?) ORDER BY loc_data"

        fun getPaperdollIndex(slot: Int): Int {
            when (slot) {
                Item.SLOT_UNDERWEAR -> return PAPERDOLL_UNDER
                Item.SLOT_R_EAR -> return PAPERDOLL_REAR
                Item.SLOT_L_EAR -> return PAPERDOLL_LEAR
                Item.SLOT_NECK -> return PAPERDOLL_NECK
                Item.SLOT_R_FINGER -> return PAPERDOLL_RFINGER
                Item.SLOT_L_FINGER -> return PAPERDOLL_LFINGER
                Item.SLOT_HEAD -> return PAPERDOLL_HEAD
                Item.SLOT_R_HAND, Item.SLOT_LR_HAND -> return PAPERDOLL_RHAND
                Item.SLOT_L_HAND -> return PAPERDOLL_LHAND
                Item.SLOT_GLOVES -> return PAPERDOLL_GLOVES
                Item.SLOT_CHEST, Item.SLOT_FULL_ARMOR, Item.SLOT_ALLDRESS -> return PAPERDOLL_CHEST
                Item.SLOT_LEGS -> return PAPERDOLL_LEGS
                Item.SLOT_FEET -> return PAPERDOLL_FEET
                Item.SLOT_BACK -> return PAPERDOLL_BACK
                Item.SLOT_FACE, Item.SLOT_HAIRALL -> return PAPERDOLL_FACE
                Item.SLOT_HAIR -> return PAPERDOLL_HAIR
            }
            return -1
        }
    }
}