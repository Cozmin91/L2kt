package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.craft.ManufactureItem
import com.l2kt.gameserver.model.craft.ManufactureList
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.RecipeShopMsg

class RequestRecipeShopListSet : L2GameClientPacket() {
    private var _count: Int = 0
    private lateinit var _items: IntArray

    override fun readImpl() {
        _count = readD()
        if (_count < 0 || _count * 8 > _buf.remaining() || _count > Config.MAX_ITEM_IN_PACKET)
            _count = 0

        _items = IntArray(_count * 2)
        for (x in 0 until _count) {
            val recipeID = readD()
            _items[x * 2 + 0] = recipeID
            val cost = readD()
            _items[x * 2 + 1] = cost
        }
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        if (player.isInDuel) {
            player.sendPacket(SystemMessageId.CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT)
            return
        }

        if (player.isInsideZone(ZoneId.NO_STORE)) {
            player.sendPacket(SystemMessageId.NO_PRIVATE_WORKSHOP_HERE)
            player.sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        if (_count == 0)
            player.forceStandUp()
        else {
            val createList = ManufactureList()

            for (x in 0 until _count) {
                val recipeID = _items[x * 2 + 0]
                val cost = _items[x * 2 + 1]
                createList.add(ManufactureItem(recipeID, cost))
            }
            createList.storeName = if (player.createList != null) player.createList!!.storeName else ""
            player.createList = createList

            player.storeType = Player.StoreType.MANUFACTURE
            player.sitDown()
            player.broadcastUserInfo()
            player.broadcastPacket(RecipeShopMsg(player))
        }
    }
}