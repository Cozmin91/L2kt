package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.data.cache.HtmCache
import com.l2kt.gameserver.data.manager.BuyListManager
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Fisherman
import com.l2kt.gameserver.model.actor.instance.Merchant
import com.l2kt.gameserver.model.holder.IntIntHolder
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ItemList
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import com.l2kt.gameserver.network.serverpackets.StatusUpdate
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestBuyItem : L2GameClientPacket() {

    private var _listId: Int = 0
    private var _items: MutableList<IntIntHolder> = mutableListOf()

    override fun readImpl() {
        _listId = readD()
        val count = readD()
        if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
            return

        for (i in 0 until count) {
            val itemId = readD()
            val cnt = readD()

            if (itemId < 1 || cnt < 1) {
                _items = mutableListOf()
                return
            }

            _items.add(i, IntIntHolder(itemId, cnt))
        }
    }

    override fun runImpl() {
        if (_items.isEmpty())
            return

        val player = client.activeChar ?: return

        // We retrieve the buylist.
        val buyList = BuyListManager.getBuyList(_listId) ?: return

        var castleTaxRate = 0.0
        var merchant: Npc? = null

        // If buylist is associated to a NPC, we retrieve the target.
        if (buyList.npcId > 0) {
            val target = player.target
            if (target is Merchant)
                merchant = target

            if (merchant == null || !buyList.isNpcAllowed(merchant.npcId) || !merchant.canInteract(player))
                return

            if (merchant.castle != null)
                castleTaxRate = merchant.castle!!.taxRate
        }

        var subTotal = 0
        var slots = 0
        var weight = 0

        for (i in _items) {
            var price: Int

            val product = buyList.getProductByItemId(i.id) ?: return

            if (!product.item.isStackable && i.value > 1) {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED))
                return
            }

            price = product.price
            if (i.id in 3960..4026)
                price *= Config.RATE_SIEGE_GUARDS_PRICE.toInt()

            if (price < 0)
                return

            if (price == 0 && !player.isGM)
                return

            if (product.hasLimitedStock()) {
                // trying to buy more then available
                if (i.value > product.count)
                    return
            }

            if (Integer.MAX_VALUE / i.value < price)
                return

            // first calculate price per item with tax, then multiply by count
            price = (price * (1 + castleTaxRate)).toInt()
            subTotal += i.value * price

            if (subTotal > Integer.MAX_VALUE)
                return

            weight += i.value * product.item.weight
            if (!product.item.isStackable)
                slots += i.value
            else if (player.inventory!!.getItemByItemId(i.id) == null)
                slots++
        }

        if (weight > Integer.MAX_VALUE || weight < 0 || !player.inventory!!.validateWeight(weight)) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED))
            return
        }

        if (slots > Integer.MAX_VALUE || slots < 0 || !player.inventory!!.validateCapacity(slots)) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SLOTS_FULL))
            return
        }

        // Charge buyer and add tax to castle treasury if not owned by npc clan
        if (subTotal < 0 || !player.reduceAdena("Buy", subTotal, player.currentFolk, false)) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA))
            return
        }

        // Proceed the purchase
        for (i in _items) {
            val product = buyList.getProductByItemId(i.id) ?: continue

            if (product.hasLimitedStock()) {
                if (product.decreaseCount(i.value))
                    player.inventory!!.addItem("Buy", i.id, i.value, player, merchant)
            } else
                player.inventory!!.addItem("Buy", i.id, i.value, player, merchant)
        }

        // Add to castle treasury and send the htm, if existing.
        if (merchant != null) {
            if (merchant.castle != null)
                merchant.castle!!.addToTreasury((subTotal * castleTaxRate).toInt())

            var htmlFolder = ""
            if (merchant is Fisherman)
                htmlFolder = "fisherman"
            else if (merchant is Merchant)
                htmlFolder = "merchant"

            if (!htmlFolder.isEmpty()) {
                val content =
                    HtmCache.getHtm("data/html/" + htmlFolder + "/" + merchant.npcId + "-bought.htm")
                if (content.isNotEmpty()) {
                    val html = NpcHtmlMessage(merchant.objectId)
                    html.setHtml(content)
                    html.replace("%objectId%", merchant.objectId)
                    player.sendPacket(html)
                }
            }
        }

        val su = StatusUpdate(player)
        su.addAttribute(StatusUpdate.CUR_LOAD, player.currentLoad)
        player.sendPacket(su)
        player.sendPacket(ItemList(player, true))
    }

    companion object {
        private const val BATCH_LENGTH = 8 // length of the one item
    }
}