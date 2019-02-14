package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.model.holder.IntIntHolder
import com.l2kt.gameserver.model.itemcontainer.ClanWarehouse
import com.l2kt.gameserver.model.itemcontainer.PcWarehouse
import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.EnchantResult
import com.l2kt.gameserver.network.serverpackets.InventoryUpdate
import com.l2kt.gameserver.network.serverpackets.StatusUpdate
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class SendWarehouseWithdrawList : L2GameClientPacket() {

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

        val folk = player.currentFolk
        if (folk == null || !folk.isWarehouse || !folk.canInteract(player))
            return

        if (warehouse !is PcWarehouse && !player.accessLevel.allowTransaction) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
            return
        }

        // Alt game - Karma punishment
        if (!Config.KARMA_PLAYER_CAN_USE_WH && player.karma > 0)
            return

        if (Config.ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH) {
            if (warehouse is ClanWarehouse && player.clanPrivileges and Clan.CP_CL_VIEW_WAREHOUSE != Clan.CP_CL_VIEW_WAREHOUSE)
                return
        } else {
            if (warehouse is ClanWarehouse && !player.isClanLeader) {
                // this msg is for depositing but maybe good to send some msg?
                player.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CAN_RETRIEVE_ITEMS_FROM_CLAN_WAREHOUSE)
                return
            }
        }

        var weight = 0
        var slots = 0

        for (i in _items) {
            // Calculate needed slots
            val item = warehouse.getItemByObjectId(i.id)
            if (item == null || item.count < i.value)
                return

            weight += i.value * item.item.weight

            if (!item.isStackable)
                slots += i.value
            else if (player.inventory!!.getItemByItemId(item.itemId) == null)
                slots++
        }

        // Item Max Limit Check
        if (!player.inventory!!.validateCapacity(slots)) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SLOTS_FULL))
            return
        }

        // Weight limit Check
        if (!player.inventory!!.validateWeight(weight)) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED))
            return
        }

        // Proceed to the transfer
        val playerIU = InventoryUpdate()
        for (i in _items) {
            val oldItem = warehouse.getItemByObjectId(i.id)
            if (oldItem == null || oldItem.count < i.value)
                return

            val newItem =
                warehouse.transferItem(warehouse.name, i.id, i.value, player.inventory, player, folk) ?: return

            if (newItem.count > i.value)
                playerIU.addModifiedItem(newItem)
            else
                playerIU.addNewItem(newItem)
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