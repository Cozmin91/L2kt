package com.l2kt.gameserver.model.itemcontainer

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.instance.Pet
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.item.type.EtcItemType

class PetInventory(public override val owner: Pet) : Inventory() {

    override val ownerId: Int
        get() {
            return owner.owner?.objectId ?: 0
        }

    override val baseLocation: ItemInstance.ItemLocation
        get() = ItemInstance.ItemLocation.PET

    override val equipLocation: ItemInstance.ItemLocation
        get() = ItemInstance.ItemLocation.PET_EQUIP

    override fun refreshWeight() {
        super.refreshWeight()

        owner.updateAndBroadcastStatus(1)
        owner.sendPetInfosToOwner()
    }

    fun validateCapacity(item: ItemInstance): Boolean {
        var slots = 0

        if (!(item.isStackable && getItemByItemId(item.itemId) != null) && item.itemType !== EtcItemType.HERB)
            slots++

        return validateCapacity(slots)
    }

    override fun validateCapacity(slots: Int): Boolean {
        return _items.size + slots <= owner.inventoryLimit
    }

    fun validateWeight(item: ItemInstance, count: Int): Boolean {
        return validateWeight(count * item.item.weight)
    }

    override fun validateWeight(weight: Int): Boolean {
        return totalWeight + weight <= owner.maxLoad
    }

    override fun deleteMe() {
        val petOwner = owner.owner
        if (petOwner != null) {
            for (item in _items) {
                if (petOwner.inventory!!.validateCapacity(1))
                    owner.transferItem("return", item.objectId, item.count, petOwner.inventory, petOwner, owner)
                else {
                    val droppedItem = dropItem("drop", item.objectId, item.count, petOwner, owner)
                    droppedItem?.dropMe(owner, owner.x + Rnd[-70, 70], owner.y + Rnd[-70, 70], owner.z + 30)
                }

            }
        }
        _items.clear()
    }
}