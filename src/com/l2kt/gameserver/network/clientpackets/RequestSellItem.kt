package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.data.cache.HtmCache
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Fisherman
import com.l2kt.gameserver.model.actor.instance.MercenaryManagerNpc
import com.l2kt.gameserver.model.actor.instance.Merchant
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.holder.IntIntHolder
import com.l2kt.gameserver.network.serverpackets.ItemList
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import com.l2kt.gameserver.network.serverpackets.StatusUpdate

class RequestSellItem : L2GameClientPacket() {

    private var _listId: Int = 0
    private lateinit var _items: Array<IntIntHolder?>

    override fun readImpl() {
        _listId = readD()
        val count = readD()
        if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
            return

        _items = arrayOfNulls(count)
        for (i in 0 until count) {
            val objectId = readD()
            val itemId = readD()
            val cnt = readD()

            if (objectId < 1 || itemId < 1 || cnt < 1) {
                _items = arrayOf()
                return
            }

            _items[i] = IntIntHolder(objectId, cnt)
        }
    }

    override fun runImpl() {
        if (_items.isEmpty())
            return

        val player = client.activeChar ?: return

        val merchant =
            if (player.target is Merchant || player.target is MercenaryManagerNpc) player.target as Npc else null
        if (merchant == null || !merchant.canInteract(player))
            return

        if (_listId > 1000000)
        // lease
        {
            if ((merchant.template as NpcTemplate).npcId != _listId - 1000000)
                return
        }

        var totalPrice = 0
        // Proceed the sell
        for (i in _items.filterNotNull()) {
            val item = player.checkItemManipulation(i.id, i.value)
            if (item == null || !item.isSellable)
                continue

            val price = item.referencePrice / 2
            totalPrice += price * i.value
            if (Integer.MAX_VALUE / i.value < price || totalPrice > Integer.MAX_VALUE)
                return

            player.inventory?.destroyItem("Sell", i.id, i.value, player, merchant)
        }

        player.addAdena("Sell", totalPrice, merchant, false)

        // Send the htm, if existing.
        var htmlFolder = ""
        if (merchant is Fisherman)
            htmlFolder = "fisherman"
        else if (merchant is Merchant)
            htmlFolder = "merchant"

        if (!htmlFolder.isEmpty()) {
            val content = HtmCache.getHtm("data/html/" + htmlFolder + "/" + merchant.npcId + "-sold.htm")
            if (content != null) {
                val html = NpcHtmlMessage(merchant.objectId)
                html.setHtml(content)
                html.replace("%objectId%", merchant.objectId)
                player.sendPacket(html)
            }
        }

        // Update current load as well
        val su = StatusUpdate(player)
        su.addAttribute(StatusUpdate.CUR_LOAD, player.currentLoad)
        player.sendPacket(su)
        player.sendPacket(ItemList(player, true))
    }

    companion object {
        private const val BATCH_LENGTH = 12 // length of the one item
    }
}