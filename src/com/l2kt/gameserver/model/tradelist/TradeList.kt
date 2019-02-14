package com.l2kt.gameserver.model.tradelist

import com.l2kt.gameserver.data.ItemTable
import com.l2kt.gameserver.model.ItemRequest
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.itemcontainer.PcInventory
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.InventoryUpdate
import com.l2kt.gameserver.network.serverpackets.StatusUpdate
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class TradeList(val owner: Player?) {
    private val _items = CopyOnWriteArrayList<TradeItem>()

    var partner: Player? = null
    var title: String? = null

    var isPackaged: Boolean = false
    var isConfirmed: Boolean = false
        private set
    var isLocked: Boolean = false
        private set

    /**
     * Retrieves items from TradeList
     * @return an array consisting of items.
     */
    val items: List<TradeItem>
        get() = _items

    /**
     * Returns the list of items in inventory available for transaction
     * @param inventory The inventory to make checks on.
     * @return ItemInstance : items in inventory
     */
    fun getAvailableItems(inventory: PcInventory): List<TradeItem> {
        val list = ArrayList<TradeItem>()
        for (item in _items) {
            val currentItem = TradeItem(item, item.count, item.price)
            inventory.adjustAvailableItem(currentItem)
            list.add(currentItem)
        }
        return list
    }

    /**
     * Adjust available item from Inventory by the one in this list
     * @param item : ItemInstance to be adjusted
     * @return TradeItem representing adjusted item
     */
    fun adjustAvailableItem(item: ItemInstance): TradeItem? {
        if (item.isStackable) {
            for (exclItem in _items) {
                if (exclItem.item.itemId == item.itemId) {
                    return if (item.count <= exclItem.count) null else TradeItem(
                        item,
                        item.count - exclItem.count,
                        item.referencePrice
                    )

                }
            }
        }
        return TradeItem(item, item.count, item.referencePrice)
    }

    /**
     * Adjust ItemRequest by corresponding item in this list using its **ObjectId**
     * @param item : ItemRequest to be adjusted
     */
    fun adjustItemRequest(item: ItemRequest) {
        for (filtItem in _items) {
            if (filtItem.objectId == item.objectId) {
                if (filtItem.count < item.count)
                    item.count = filtItem.count

                return
            }
        }
        item.count = 0
    }

    /**
     * Add simplified item to TradeList
     * @param objectId : int
     * @param count : int
     * @return
     */
    fun addItem(objectId: Int, count: Int): TradeItem? {
        return addItem(objectId, count, 0)
    }

    /**
     * Add item to TradeList
     * @param objectId : int
     * @param count : int
     * @param price : int
     * @return
     */
    @Synchronized
    fun addItem(objectId: Int, count: Int, price: Int): TradeItem? {
        if (isLocked)
            return null

        val o = World.getInstance().getObject(objectId) as? ItemInstance ?: return null

        if (!o.isTradable || o.isQuestItem)
            return null

        if (count <= 0 || count > o.count)
            return null

        if (!o.isStackable && count > 1)
            return null

        if (Integer.MAX_VALUE / count < price)
            return null

        for (checkitem in _items) {
            if (checkitem.objectId == objectId)
                return null
        }

        val titem = TradeItem(o, count, price)
        _items.add(titem)

        // If Player has already confirmed this trade, invalidate the confirmation
        invalidateConfirmation()
        return titem
    }

    /**
     * Add item to TradeList
     * @param itemId : int
     * @param count : int
     * @param price : int
     * @return
     */
    @Synchronized
    fun addItemByItemId(itemId: Int, count: Int, price: Int): TradeItem? {
        if (isLocked)
            return null

        val item = ItemTable.getTemplate(itemId) ?: return null

        if (!item.isTradable || item.isQuestItem)
            return null

        if (!item.isStackable && count > 1)
            return null

        if (Integer.MAX_VALUE / count < price)
            return null

        val titem = TradeItem(item, count, price)
        _items.add(titem)

        // If Player has already confirmed this trade, invalidate the confirmation
        invalidateConfirmation()
        return titem
    }

    /**
     * Remove item from TradeList
     * @param objectId : int
     * @param itemId : int
     * @param count : int
     * @return
     */
    @Synchronized
    fun removeItem(objectId: Int, itemId: Int, count: Int): TradeItem? {
        if (isLocked)
            return null

        for (titem in _items) {
            if (titem.objectId == objectId || titem.item.itemId == itemId) {
                // If Partner has already confirmed this trade, invalidate the confirmation
                if (partner != null) {
                    val partnerList = partner!!.activeTradeList ?: return null

                    partnerList.invalidateConfirmation()
                }

                // Reduce item count or complete item
                if (count != -1 && titem.count > count)
                    titem.count = titem.count - count
                else
                    _items.remove(titem)

                return titem
            }
        }
        return null
    }

    /**
     * Update items in TradeList according their quantity in owner inventory
     */
    @Synchronized
    fun updateItems() {
        for (titem in _items) {
            val item = owner!!.inventory!!.getItemByObjectId(titem.objectId)
            if (item == null || titem.count < 1)
                removeItem(titem.objectId, -1, -1)
            else if (item.count < titem.count)
                titem.count = item.count
        }
    }

    /**
     * Lockes TradeList, no further changes are allowed
     */
    fun lock() {
        isLocked = true
    }

    /**
     * Clears item list
     */
    @Synchronized
    fun clear() {
        _items.clear()
        isLocked = false
    }

    /**
     * Confirms TradeList
     * @return : boolean
     */
    fun confirm(): Boolean {
        if (isConfirmed)
            return true // Already confirmed

        // If Partner has already confirmed this trade, proceed exchange
        if (partner != null) {
            val partnerList = partner!!.activeTradeList ?: return false

            // Synchronization order to avoid deadlock
            val sync1: TradeList
            val sync2: TradeList
            if (owner!!.objectId > partnerList.owner!!.objectId) {
                sync1 = partnerList
                sync2 = this
            } else {
                sync1 = this
                sync2 = partnerList
            }

            synchronized(sync1) {
                synchronized(sync2) {
                    isConfirmed = true
                    if (partnerList.isConfirmed) {
                        partnerList.lock()
                        lock()

                        if (!partnerList.validate())
                            return false

                        if (!validate())
                            return false

                        doExchange(partnerList)
                    } else
                        partner!!.onTradeConfirm(owner)
                }
            }
        } else
            isConfirmed = true

        return isConfirmed
    }

    /**
     * Cancels TradeList confirmation
     */
    fun invalidateConfirmation() {
        isConfirmed = false
    }

    /**
     * Validates TradeList with owner inventory
     * @return true if ok, false otherwise.
     */
    private fun validate(): Boolean {
        // Check for Owner validity
        if (owner == null || World.getInstance().getPlayer(owner.objectId) == null)
            return false

        // Check for Item validity
        for (titem in _items) {
            val item = owner.checkItemManipulation(titem.objectId, titem.count) ?: return false
        }
        return true
    }

    /**
     * Transfers all TradeItems from inventory to partner
     * @param partner
     * @param ownerIU
     * @param partnerIU
     * @return true if ok, false otherwise.
     */
    private fun transferItems(partner: Player?, ownerIU: InventoryUpdate?, partnerIU: InventoryUpdate?): Boolean {
        for (titem in _items) {
            val oldItem = owner!!.inventory!!.getItemByObjectId(titem.objectId) ?: return false

            val newItem = owner.inventory!!.transferItem(
                "Trade",
                titem.objectId,
                titem.count,
                partner!!.inventory,
                owner,
                this.partner
            ) ?: return false

            // Add changes to inventory update packets
            if (ownerIU != null) {
                if (oldItem.count > 0 && oldItem != newItem)
                    ownerIU.addModifiedItem(oldItem)
                else
                    ownerIU.addRemovedItem(oldItem)
            }

            if (partnerIU != null) {
                if (newItem.count > titem.count)
                    partnerIU.addModifiedItem(newItem)
                else
                    partnerIU.addNewItem(newItem)
            }
        }
        return true
    }

    /**
     * Count items slots
     * @param partner
     * @return
     */
    fun countItemsSlots(partner: Player?): Int {
        var slots = 0

        for (item in _items) {
            if (item == null)
                continue

            val template = ItemTable.getTemplate(item.item.itemId) ?: continue

            if (!template.isStackable)
                slots += item.count
            else if (partner!!.inventory!!.getItemByItemId(item.item.itemId) == null)
                slots++
        }
        return slots
    }

    /**
     * @return weight of items in tradeList
     */
    fun calcItemsWeight(): Int {
        var weight = 0

        for (item in _items) {
            if (item == null)
                continue

            val template = ItemTable.getTemplate(item.item.itemId) ?: continue

            weight += item.count * template.weight
        }
        return Math.min(weight, Integer.MAX_VALUE)
    }

    /**
     * Proceeds with trade
     * @param partnerList
     */
    private fun doExchange(partnerList: TradeList) {
        var success = false

        // check weight and slots
        if (!owner!!.inventory!!.validateWeight(partnerList.calcItemsWeight()) || !partnerList.owner!!.inventory!!.validateWeight(
                calcItemsWeight()
            )
        ) {
            partnerList.owner!!.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED)
            owner.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED)
        } else if (!owner.inventory!!.validateCapacity(partnerList.countItemsSlots(owner)) || !partnerList.owner.inventory!!.validateCapacity(
                countItemsSlots(partnerList.owner)
            )
        ) {
            partnerList.owner.sendPacket(SystemMessageId.SLOTS_FULL)
            owner.sendPacket(SystemMessageId.SLOTS_FULL)
        } else {
            // Prepare inventory update packet
            val ownerIU = InventoryUpdate()
            val partnerIU = InventoryUpdate()

            // Transfer items
            partnerList.transferItems(owner, partnerIU, ownerIU)
            transferItems(partnerList.owner, ownerIU, partnerIU)

            // Send inventory update packet
            owner.sendPacket(ownerIU)
            partner!!.sendPacket(partnerIU)

            // Update current load as well
            var playerSU = StatusUpdate(owner)
            playerSU.addAttribute(StatusUpdate.CUR_LOAD, owner.currentLoad)
            owner.sendPacket(playerSU)
            playerSU = StatusUpdate(partner!!)
            playerSU.addAttribute(StatusUpdate.CUR_LOAD, partner!!.currentLoad)
            partner!!.sendPacket(playerSU)

            success = true
        }
        // Finish the trade
        partnerList.owner.onTradeFinish(success)
        owner.onTradeFinish(success)
    }

    /**
     * Buy items from this PrivateStore list
     * @param player
     * @param items
     * @return true if successful, false otherwise.
     */
    @Synchronized
    fun privateStoreBuy(player: Player, items: Set<ItemRequest>): Boolean {
        if (isLocked)
            return false

        if (!validate()) {
            lock()
            return false
        }

        if (!owner!!.isOnline || !player.isOnline)
            return false

        var slots = 0
        var weight = 0
        var totalPrice = 0

        val ownerInventory = owner.inventory
        val playerInventory = player.inventory

        for (item in items) {
            var found = false

            for (ti in _items) {
                if (ti.objectId == item.objectId) {
                    if (ti.price == item.price) {
                        if (ti.count < item.count)
                            item.count = ti.count
                        found = true
                    }
                    break
                }
            }
            // item with this objectId and price not found in tradelist
            if (!found) {
                if (isPackaged)
                    return false

                item.count = 0
                continue
            }

            // check for overflow in the single item
            if (Integer.MAX_VALUE / item.count < item.price) {
                // private store attempting to overflow - disable it
                lock()
                return false
            }

            totalPrice += item.count * item.price
            // check for overflow of the total price
            if (Integer.MAX_VALUE < totalPrice || totalPrice < 0) {
                // private store attempting to overflow - disable it
                lock()
                return false
            }

            // Check if requested item is available for manipulation
            val oldItem = owner.checkItemManipulation(item.objectId, item.count)
            if (oldItem == null || !oldItem.isTradable) {
                // private store sell invalid item - disable it
                lock()
                return false
            }

            val template = ItemTable.getTemplate(item.itemId) ?: continue
            weight += item.count * template.weight
            if (!template.isStackable)
                slots += item.count
            else if (playerInventory!!.getItemByItemId(item.itemId) == null)
                slots++
        }

        if (totalPrice > playerInventory!!.adena) {
            player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA)
            return false
        }

        if (!playerInventory.validateWeight(weight)) {
            player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED)
            return false
        }

        if (!playerInventory.validateCapacity(slots)) {
            player.sendPacket(SystemMessageId.SLOTS_FULL)
            return false
        }

        // Prepare inventory update packets
        val ownerIU = InventoryUpdate()
        val playerIU = InventoryUpdate()

        val adenaItem = playerInventory.adenaInstance
        if (!playerInventory.reduceAdena("PrivateStore", totalPrice, player, owner)) {
            player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA)
            return false
        }

        playerIU.addItem(adenaItem)
        ownerInventory!!.addAdena("PrivateStore", totalPrice, owner, player)

        var ok = true

        // Transfer items
        for ((objectId, count) in items) {
            if (count == 0)
                continue

            // Check if requested item is available for manipulation
            val oldItem = owner.checkItemManipulation(objectId, count)
            if (oldItem == null) {
                // should not happens - validation already done
                lock()
                ok = false
                break
            }

            // Proceed with item transfer
            val newItem = ownerInventory.transferItem("PrivateStore", objectId, count, playerInventory, owner, player)
            if (newItem == null) {
                ok = false
                break
            }
            removeItem(objectId, -1, count)

            // Add changes to inventory update packets
            if (oldItem.count > 0 && oldItem != newItem)
                ownerIU.addModifiedItem(oldItem)
            else
                ownerIU.addRemovedItem(oldItem)
            if (newItem.count > count)
                playerIU.addModifiedItem(newItem)
            else
                playerIU.addNewItem(newItem)

            // Send messages about the transaction to both players
            if (newItem.isStackable) {
                var msg = SystemMessage.getSystemMessage(SystemMessageId.S1_PURCHASED_S3_S2_S)
                msg.addString(player.name)
                msg.addItemName(newItem.itemId)
                msg.addNumber(count)
                owner.sendPacket(msg)

                msg = SystemMessage.getSystemMessage(SystemMessageId.PURCHASED_S3_S2_S_FROM_S1)
                msg.addString(owner.name)
                msg.addItemName(newItem.itemId)
                msg.addNumber(count)
                player.sendPacket(msg)
            } else {
                var msg = SystemMessage.getSystemMessage(SystemMessageId.S1_PURCHASED_S2)
                msg.addString(player.name)
                msg.addItemName(newItem.itemId)
                owner.sendPacket(msg)

                msg = SystemMessage.getSystemMessage(SystemMessageId.PURCHASED_S2_FROM_S1)
                msg.addString(owner.name)
                msg.addItemName(newItem.itemId)
                player.sendPacket(msg)
            }
        }

        // Send inventory update packet
        owner.sendPacket(ownerIU)
        player.sendPacket(playerIU)
        return if (ok) true else false

    }

    /**
     * Sell items to this PrivateStore list
     * @param player
     * @param items
     * @return true if successful, false otherwise.
     */
    @Synchronized
    fun privateStoreSell(player: Player, items: List<ItemRequest>): Boolean {
        if (isLocked)
            return false

        if (!owner!!.isOnline || !player.isOnline)
            return false

        var ok = false

        val ownerInventory = owner.inventory
        val playerInventory = player.inventory

        // Prepare inventory update packet
        val ownerIU = InventoryUpdate()
        val playerIU = InventoryUpdate()

        var totalPrice = 0

        for (item in items) {
            // searching item in tradelist using itemId
            var found = false

            for (ti in _items) {
                if (ti.item.itemId == item.itemId) {
                    // price should be the same
                    if (ti.price == item.price) {
                        // if requesting more than available - decrease count
                        if (ti.count < item.count)
                            item.count = ti.count
                        found = item.count > 0
                    }
                    break
                }
            }
            // not found any item in the tradelist with same itemId and price
            // maybe another player already sold this item ?
            if (!found)
                continue

            // check for overflow in the single item
            if (Integer.MAX_VALUE / item.count < item.price) {
                lock()
                break
            }

            val _totalPrice = totalPrice + item.count * item.price
            // check for overflow of the total price
            if (Integer.MAX_VALUE < _totalPrice || _totalPrice < 0) {
                lock()
                break
            }

            if (ownerInventory!!.adena < _totalPrice)
                continue

            // Check if requested item is available for manipulation
            var objectId = item.objectId
            var oldItem = player.checkItemManipulation(objectId, item.count)
            // private store - buy use same objectId for buying several non-stackable items
            if (oldItem == null) {
                // searching other items using same itemId
                oldItem = playerInventory!!.getItemByItemId(item.itemId)
                if (oldItem == null)
                    continue

                objectId = oldItem.objectId
                oldItem = player.checkItemManipulation(objectId, item.count)
                if (oldItem == null)
                    continue
            }

            if (oldItem.itemId != item.itemId)
                return false

            if (!oldItem.isTradable)
                continue

            // Proceed with item transfer
            val newItem =
                playerInventory!!.transferItem("PrivateStore", objectId, item.count, ownerInventory, player, owner)
                    ?: continue

            removeItem(-1, item.itemId, item.count)
            ok = true

            // increase total price only after successful transaction
            totalPrice = _totalPrice

            // Add changes to inventory update packets
            if (oldItem.count > 0 && oldItem != newItem)
                playerIU.addModifiedItem(oldItem)
            else
                playerIU.addRemovedItem(oldItem)
            if (newItem.count > item.count)
                ownerIU.addModifiedItem(newItem)
            else
                ownerIU.addNewItem(newItem)

            // Send messages about the transaction to both players
            if (newItem.isStackable) {
                var msg = SystemMessage.getSystemMessage(SystemMessageId.PURCHASED_S3_S2_S_FROM_S1)
                msg.addString(player.name)
                msg.addItemName(newItem.itemId)
                msg.addNumber(item.count)
                owner.sendPacket(msg)

                msg = SystemMessage.getSystemMessage(SystemMessageId.S1_PURCHASED_S3_S2_S)
                msg.addString(owner.name)
                msg.addItemName(newItem.itemId)
                msg.addNumber(item.count)
                player.sendPacket(msg)
            } else {
                var msg = SystemMessage.getSystemMessage(SystemMessageId.PURCHASED_S2_FROM_S1)
                msg.addString(player.name)
                msg.addItemName(newItem.itemId)
                owner.sendPacket(msg)

                msg = SystemMessage.getSystemMessage(SystemMessageId.S1_PURCHASED_S2)
                msg.addString(owner.name)
                msg.addItemName(newItem.itemId)
                player.sendPacket(msg)
            }
        }

        // Transfer adena
        if (totalPrice > 0) {
            if (totalPrice > ownerInventory!!.adena)
                return false

            val adenaItem = ownerInventory.adenaInstance
            ownerInventory.reduceAdena("PrivateStore", totalPrice, owner, player)
            ownerIU.addItem(adenaItem)

            playerInventory!!.addAdena("PrivateStore", totalPrice, player, owner)
            playerIU.addItem(playerInventory.adenaInstance)
        }

        if (ok) {
            // Send inventory update packet
            owner.sendPacket(ownerIU)
            player.sendPacket(playerIU)
        }
        return ok
    }
}