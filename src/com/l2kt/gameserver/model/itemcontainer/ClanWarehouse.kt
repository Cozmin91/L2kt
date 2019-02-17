package com.l2kt.gameserver.model.itemcontainer

import com.l2kt.Config
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.pledge.Clan

class ClanWarehouse(private val _clan: Clan) : ItemContainer() {

    override val name: String
        get() = "ClanWarehouse"

    override val ownerId: Int
        get() = _clan.clanId

    public override val owner: Player?
        get() = _clan.leader?.playerInstance

    public override val baseLocation: ItemInstance.ItemLocation
        get() = ItemInstance.ItemLocation.CLANWH

    override fun validateCapacity(slots: Int): Boolean {
        return _items.size + slots <= Config.WAREHOUSE_SLOTS_CLAN
    }
}