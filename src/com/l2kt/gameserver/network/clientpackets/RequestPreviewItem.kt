package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.data.manager.BuyListManager
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Merchant
import com.l2kt.gameserver.model.itemcontainer.Inventory
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.ShopPreviewInfo
import com.l2kt.gameserver.network.serverpackets.UserInfo

class RequestPreviewItem : L2GameClientPacket() {
    private var _itemList: MutableMap<Int, Int> = mutableMapOf()
    private var _unk: Int = 0
    private var _listId: Int = 0
    private var _count: Int = 0
    private lateinit var _items: IntArray

    override fun readImpl() {
        _unk = readD()
        _listId = readD()
        _count = readD()

        if (_count < 0)
            _count = 0
        else if (_count > 100)
            return  // prevent too long lists

        // Create _items table that will contain all ItemID to Wear
        _items = IntArray(_count)

        // Fill _items table with all ItemID to Wear
        for (i in 0 until _count)
            _items[i] = readD()
    }

    override fun runImpl() {
        if(_items.isEmpty())
            return

        if (_count < 1 || _listId >= 4000000) {
            sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        // Get the current player and return if null
        val activeChar = client.activeChar ?: return

        // Check current target of the player and the INTERACTION_DISTANCE
        val target = activeChar.target
        if (!activeChar.isGM && (target == null || target !is Merchant || !activeChar.isInsideRadius(
                target,
                Npc.INTERACTION_DISTANCE,
                false,
                false
            ))
        )
            return

        // Get the current merchant targeted by the player
        val merchant = (if (target is Merchant) target else null) ?: return

        val buyList = BuyListManager.getInstance().getBuyList(_listId) ?: return

        var totalPrice = 0
        _listId = buyList.listId

        for (i in 0 until _count) {
            val itemId = _items!![i]

            val product = buyList.getProductByItemId(itemId) ?: return

            val template = product.item ?: continue

            val slot = Inventory.getPaperdollIndex(template.bodyPart)
            if (slot < 0)
                continue

            if (_itemList!!.containsKey(slot)) {
                activeChar.sendPacket(SystemMessageId.YOU_CAN_NOT_TRY_THOSE_ITEMS_ON_AT_THE_SAME_TIME)
                return
            }
            _itemList!![slot] = itemId

            totalPrice += Config.WEAR_PRICE
            if (totalPrice > Integer.MAX_VALUE)
                return
        }

        // Charge buyer and add tax to castle treasury if not owned by npc clan because a Try On is not Free
        if (totalPrice < 0 || !activeChar.reduceAdena("Wear", totalPrice, activeChar.currentFolk, true)) {
            activeChar.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA)
            return
        }

        if (!_itemList!!.isEmpty()) {
            activeChar.sendPacket(ShopPreviewInfo(_itemList!!))

            // Schedule task
            ThreadPool.schedule({
                activeChar.sendPacket(SystemMessageId.NO_LONGER_TRYING_ON)
                activeChar.sendPacket(UserInfo(activeChar))
            }, (Config.WEAR_DELAY * 1000).toLong())
        }
    }
}