package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.data.ItemTable
import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.data.manager.CastleManorManager
import com.l2kt.gameserver.model.actor.instance.ManorManagerNpc
import com.l2kt.gameserver.model.holder.IntIntHolder
import com.l2kt.gameserver.model.manor.SeedProduction
import com.l2kt.gameserver.network.FloodProtectors
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.util.*

class RequestBuySeed : L2GameClientPacket() {

    private var _manorId: Int = 0
    private var _items: MutableList<IntIntHolder> = mutableListOf()

    override fun readImpl() {
        _manorId = readD()

        val count = readD()
        if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
            return

        for (i in 0 until count) {
            val itemId = readD()
            val cnt = readD()

            if (cnt < 1 || itemId < 1) {
                _items = mutableListOf()
                return
            }

            _items.add(i, IntIntHolder(itemId, cnt))
        }
    }

    override fun runImpl() {
        if (!FloodProtectors.performAction(client, FloodProtectors.Action.MANOR))
            return

        val player = client.activeChar ?: return

        if (_items.isEmpty()) {
            sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        val manor = CastleManorManager
        if (manor.isUnderMaintenance) {
            sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        val castle = CastleManager.getCastleById(_manorId)
        if (castle == null) {
            sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        val folk = player.currentFolk
        if (folk !is ManorManagerNpc || !folk.canInteract(player) || folk.getCastle() !== castle) {
            sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        var totalPrice = 0
        var slots = 0
        var totalWeight = 0

        val _productInfo = HashMap<Int, SeedProduction>()

        for (ih in _items) {
            val sp = manor.getSeedProduct(_manorId, ih.id, false)
            if (sp == null || sp.price <= 0 || sp.amount < ih.value || Integer.MAX_VALUE / ih.value < sp.price) {
                sendPacket(ActionFailed.STATIC_PACKET)
                return
            }

            // Calculate price
            totalPrice += sp.price * ih.value
            if (totalPrice > Integer.MAX_VALUE) {
                sendPacket(ActionFailed.STATIC_PACKET)
                return
            }

            val template = ItemTable.getTemplate(ih.id)
            totalWeight += ih.value * template!!.weight

            // Calculate slots
            if (!template.isStackable)
                slots += ih.value
            else if (player.inventory!!.getItemByItemId(ih.id) == null)
                slots++

            _productInfo[ih.id] = sp
        }

        if (!player.inventory!!.validateWeight(totalWeight)) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED))
            return
        }

        if (!player.inventory!!.validateCapacity(slots)) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SLOTS_FULL))
            return
        }

        if (totalPrice < 0 || player.adena < totalPrice) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA))
            return
        }

        // Proceed the purchase
        for (i in _items) {
            val sp = _productInfo[i.id]!!
            val price = sp.price * i.value

            // Take Adena and decrease seed amount
            if (!sp.decreaseAmount(i.value) || !player.reduceAdena("Buy", price, player, false)) {
                // failed buy, reduce total price
                totalPrice -= price
                continue
            }

            // Add item to player's inventory
            player.addItem("Buy", i.id, i.value, folk, true)
        }

        // Adding to treasury for Manor Castle
        if (totalPrice > 0) {
            castle.addToTreasuryNoTax(totalPrice.toLong())
            player.sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED_ADENA).addItemNumber(
                    totalPrice
                )
            )
        }
    }

    companion object {
        private val BATCH_LENGTH = 8
    }
}