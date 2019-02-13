package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.model.holder.IntIntHolder
import com.l2kt.gameserver.model.itemcontainer.PcInventory
import com.l2kt.gameserver.model.itemcontainer.PcWarehouse
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.EnchantResult
import com.l2kt.gameserver.network.serverpackets.InventoryUpdate
import com.l2kt.gameserver.network.serverpackets.StatusUpdate
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class SendWarehouseDepositList : L2GameClientPacket() {

    private var _items: MutableList<IntIntHolder> = mutableListOf()

    override fun readImpl() {
        val count = readD()
        if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
            return

        for (i in 0 until count) {
            val objId = readD()
            val cnt = readD()

            if (objId < 1 || cnt < 0) {
                _items = mutableListOf()
                return
            }
            _items.add(i, IntIntHolder(objId, cnt))
        }
    }

    override fun runImpl() {
        if (_items.isEmpty())
            return

        val player = client.activeChar ?: return

        if (player.isProcessingTransaction) {
            player.sendPacket(SystemMessageId.ALREADY_TRADING)
            return
        }

        if (player.activeEnchantItem != null) {
            player.activeEnchantItem = null
            player.sendPacket(EnchantResult.CANCELLED)
            player.sendPacket(SystemMessageId.ENCHANT_SCROLL_CANCELLED)
        }

        val warehouse = player.activeWarehouse ?: return

        val isPrivate = warehouse is PcWarehouse

        val folk = player.currentFolk
        if (folk == null || !folk.isWarehouse || !folk.canInteract(player))
            return

        if (!isPrivate && !player.accessLevel.allowTransaction()) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
            return
        }

        // Alt game - Karma punishment
        if (!Config.KARMA_PLAYER_CAN_USE_WH && player.karma > 0)
            return

        // Freight price from config or normal price per item slot (30)
        val fee = _items.size * 30
        var currentAdena = player.adena
        var slots = 0

        for (i in _items) {
            val item = player.checkItemManipulation(i.id, i.value) ?: return

            // Calculate needed adena and slots
            if (item.itemId == PcInventory.ADENA_ID)
                currentAdena -= i.value

            if (!item.isStackable)
                slots += i.value
            else if (warehouse.getItemByItemId(item.itemId) == null)
                slots++
        }

        // Item Max Limit Check
        if (!warehouse.validateCapacity(slots)) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED))
            return
        }

        // Check if enough adena and charge the fee
        if (currentAdena < fee || !player.reduceAdena(warehouse.name, fee, folk, false)) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA))
            return
        }

        // get current tradelist if any
        if (player.activeTradeList != null)
            return

        // Proceed to the transfer
        val playerIU = InventoryUpdate()
        for (i in _items) {
            // Check validity of requested item
            val oldItem = player.checkItemManipulation(i.id, i.value) ?: return

            if (!oldItem.isDepositable(isPrivate) || !oldItem.isAvailable(player, true, isPrivate))
                continue

            val newItem =
                player.inventory!!.transferItem(warehouse.name, i.id, i.value, warehouse, player, folk) ?: continue

            if (oldItem.count > 0 && oldItem != newItem)
                playerIU.addModifiedItem(oldItem)
            else
                playerIU.addRemovedItem(oldItem)
        }

        // Send updated item list to the player
        player.sendPacket(playerIU)

        // Update current load status on player
        val su = StatusUpdate(player)
        su.addAttribute(StatusUpdate.CUR_LOAD, player.currentLoad)
        player.sendPacket(su)
    }

    companion object {
        private const val BATCH_LENGTH = 8 // length of one item
    }
}