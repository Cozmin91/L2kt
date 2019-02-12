package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.holder.IntIntHolder
import com.l2kt.gameserver.model.itemcontainer.PcFreight
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.InventoryUpdate
import com.l2kt.gameserver.network.serverpackets.StatusUpdate
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestPackageSend : L2GameClientPacket() {
    private var _items: MutableList<IntIntHolder> = mutableListOf()
    private var _objectID: Int = 0

    override fun readImpl() {
        _objectID = readD()

        val count = readD()
        if (count < 0 || count > Config.MAX_ITEM_IN_PACKET)
            return

        for (i in 0 until count) {
            val id = readD()
            val cnt = readD()

            _items.add(i, IntIntHolder(id, cnt))
        }
    }

    override fun runImpl() {
        if (_items.isEmpty() || !Config.ALLOW_FREIGHT)
            return

        val player = client.activeChar ?: return

        // player attempts to send freight to the different account
        if (!player.accountChars.containsKey(_objectID))
            return

        val freight = player.getDepositedFreight(_objectID)
        player.activeWarehouse = freight

        val warehouse = player.activeWarehouse ?: return

        val folk = player.currentFolk
        if ((folk == null || !player.isInsideRadius(folk, Npc.INTERACTION_DISTANCE, false, false)) && !player.isGM)
            return

        if (warehouse is PcFreight && !player.accessLevel.allowTransaction()) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
            return
        }

        // Alt game - Karma punishment
        if (!Config.KARMA_PLAYER_CAN_USE_WH && player.karma > 0)
            return

        // Freight price from config or normal price per item slot (30)
        val fee = _items.size * Config.ALT_GAME_FREIGHT_PRICE
        var currentAdena = player.adena
        var slots = 0

        for (i in _items) {
            val count = i.value

            // Check validity of requested item
            val item = player.checkItemManipulation(i.id, count)
            if (item == null) {
                i.id = 0
                i.value = 0
                continue
            }

            if (!item.isTradable || item.isQuestItem)
                return

            // Calculate needed adena and slots
            if (item.itemId == 57)
                currentAdena -= count

            if (!item.isStackable)
                slots += count
            else if (warehouse.getItemByItemId(item.itemId) == null)
                slots++
        }

        // Item Max Limit Check
        if (!warehouse.validateCapacity(slots)) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED))
            return
        }

        // Check if enough adena and charge the fee
        if (currentAdena < fee || !player.reduceAdena("Warehouse", fee, player.currentFolk, false)) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA))
            return
        }

        // Proceed to the transfer
        val playerIU = InventoryUpdate()
        for (i in _items!!) {
            val objectId = i.id
            val count = i.value

            // check for an invalid item
            if (objectId == 0 && count == 0)
                continue

            val oldItem = player.inventory!!.getItemByObjectId(objectId)
            if (oldItem == null || oldItem.isHeroItem)
                continue

            val newItem =
                player.inventory!!.transferItem("Warehouse", objectId, count, warehouse, player, player.currentFolk)
                    ?: continue

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
}