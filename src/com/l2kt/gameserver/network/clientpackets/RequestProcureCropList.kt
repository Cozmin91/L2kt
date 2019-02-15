package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.data.ItemTable
import com.l2kt.gameserver.data.manager.CastleManorManager
import com.l2kt.gameserver.model.actor.instance.ManorManagerNpc
import com.l2kt.gameserver.model.holder.IntIntHolder
import com.l2kt.gameserver.model.manor.CropProcure
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.util.*

class RequestProcureCropList : L2GameClientPacket() {

    private var _items: MutableList<CropHolder> = mutableListOf()

    override fun readImpl() {
        val count = readD()
        if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
            return

        _items = ArrayList(count)
        for (i in 0 until count) {
            val objId = readD()
            val itemId = readD()
            val manorId = readD()
            val cnt = readD()

            if (objId < 1 || itemId < 1 || manorId < 0 || cnt < 0) {
                _items = mutableListOf()
                return
            }

            _items.add(CropHolder(objId, itemId, cnt, manorId))
        }
    }

    override fun runImpl() {
        if (_items.isEmpty())
            return

        val player = client.activeChar ?: return

        val manor = CastleManorManager
        if (manor.isUnderMaintenance) {
            sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        val folk = player.currentFolk
        if (folk !is ManorManagerNpc || !folk.canInteract(player)) {
            sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        val castleId = folk.getCastle().castleId

        // Calculate summary values
        var slots = 0
        var weight = 0

        for (i in _items) {
            val item = player.inventory!!.getItemByObjectId(i.objectId)
            if (item == null || item.count < i.value || item.itemId != i.id) {
                sendPacket(ActionFailed.STATIC_PACKET)
                return
            }

            val cp = i.cropProcure
            if (cp == null || cp.amount < i.value) {
                sendPacket(ActionFailed.STATIC_PACKET)
                return
            }

            val template = ItemTable.getTemplate(i.rewardId)
            weight += i.value * template!!.weight

            if (!template.isStackable)
                slots += i.value
            else if (player.inventory!!.getItemByItemId(i.rewardId) == null)
                slots++
        }

        if (!player.inventory!!.validateWeight(weight)) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED))
            return
        }

        if (!player.inventory!!.validateCapacity(slots)) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SLOTS_FULL))
            return
        }

        // Proceed the purchase
        for (i in _items) {
            val rewardPrice = ItemTable.getTemplate(i.rewardId)!!.referencePrice
            if (rewardPrice == 0)
                continue

            val rewardItemCount = i.price / rewardPrice
            if (rewardItemCount < 1) {
                player.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.FAILED_IN_TRADING_S2_OF_CROP_S1).addItemName(
                        i.id
                    ).addItemNumber(i.value)
                )
                continue
            }

            // Fee for selling to other manors
            val fee = if (castleId == i.manorId) 0 else (i.price * 0.05).toInt()
            if (fee != 0 && player.adena < fee) {
                sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.FAILED_IN_TRADING_S2_OF_CROP_S1).addItemName(i.id).addItemNumber(
                        i.value
                    )
                )
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA))
                continue
            }

            val cp = i.cropProcure
            if (!cp!!.decreaseAmount(i.value) || fee > 0 && !player.reduceAdena(
                    "Manor",
                    fee,
                    folk,
                    true
                ) || !player.destroyItem("Manor", i.objectId, i.value, folk, true)
            )
                continue

            player.addItem("Manor", i.rewardId, rewardItemCount, folk, true)
        }
    }

    private inner class CropHolder(val objectId: Int, id: Int, count: Int, val manorId: Int) : IntIntHolder(id, count) {

        private var _cp: CropProcure? = null
        private var _rewardId = 0

        val price: Int
            get() = value * _cp!!.price

        val cropProcure: CropProcure?
            get() {
                if (_cp == null)
                    _cp = CastleManorManager.getCropProcure(manorId, id, false)

                return _cp
            }

        val rewardId: Int
            get() {
                if (_rewardId == 0)
                    _rewardId = CastleManorManager.getSeedByCrop(_cp!!.id)!!.getReward(_cp!!.reward)

                return _rewardId
            }
    }

    companion object {
        private const val BATCH_LENGTH = 16
    }
}