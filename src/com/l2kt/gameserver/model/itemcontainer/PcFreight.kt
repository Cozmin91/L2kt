package com.l2kt.gameserver.model.itemcontainer

import com.l2kt.Config
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance

class PcFreight(public override val owner: Player?) : ItemContainer() {

    private var _activeLocationId: Int = 0
    private var _tempOwnerId = 0

    override val name: String
        get() = "Freight"

    public override val baseLocation: ItemInstance.ItemLocation
        get() = ItemInstance.ItemLocation.FREIGHT

    override val size: Int
        get() {
            var size = 0
            for (item in _items) {
                if (item.locationSlot == 0 || _activeLocationId == 0 || item.locationSlot == _activeLocationId)
                    size++
            }
            return size
        }

    override val items: Set<ItemInstance>
        get() = if (_items.isEmpty()) emptySet() else _items
            .filter { i -> i.locationSlot == 0 || i.locationSlot == _activeLocationId }.toSet()

    override val ownerId: Int
        get() = if (owner == null) _tempOwnerId else super.ownerId

    fun setActiveLocation(locationId: Int) {
        _activeLocationId = locationId
    }

    override fun getItemByItemId(itemId: Int): ItemInstance? {
        for (item in _items) {
            if (item.itemId == itemId && (item.locationSlot == 0 || _activeLocationId == 0 || item.locationSlot == _activeLocationId))
                return item
        }
        return null
    }

    override fun addItem(item: ItemInstance) {
        super.addItem(item)

        if (_activeLocationId > 0)
            item.setLocation(item.location, _activeLocationId)
    }

    override fun restore() {
        val locationId = _activeLocationId
        _activeLocationId = 0

        super.restore()

        _activeLocationId = locationId
    }

    override fun validateCapacity(slots: Int): Boolean {
        return size + slots <= owner?.freightLimit ?: Config.FREIGHT_SLOTS
    }

    /**
     * This provides support to load a new PcFreight without owner so that transactions can be done
     * @param val The id of the owner.
     */
    fun doQuickRestore(`val`: Int) {
        _tempOwnerId = `val`

        restore()
    }
}