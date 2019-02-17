package com.l2kt.gameserver.model.itemcontainer

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance

class PcWarehouse(public override val owner: Player) : ItemContainer() {

    override val name: String
        get() = "Warehouse"

    public override val baseLocation: ItemInstance.ItemLocation
        get() = ItemInstance.ItemLocation.WAREHOUSE

    override fun validateCapacity(slots: Int): Boolean {
        return _items.size + slots <= owner.wareHouseLimit
    }
}