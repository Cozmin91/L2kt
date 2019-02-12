package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.model.ItemRequest
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId

class RequestPrivateStoreBuy : L2GameClientPacket() {

    private var _storePlayerId: Int = 0
    private var _items: MutableSet<ItemRequest> = mutableSetOf()

    override fun readImpl() {
        _storePlayerId = readD()
        val count = readD()
        if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
            return

        for (i in 0 until count) {
            val objectId = readD()
            val cnt = readD().toLong()
            val price = readD()

            if (objectId < 1 || cnt < 1 || price < 0) {
                return
            }

            _items.add(ItemRequest(objectId, cnt.toInt(), price))
        }
    }

    override fun runImpl() {
        if (_items.isEmpty())
            return

        val player = client.activeChar ?: return

        if (player.isCursedWeaponEquipped)
            return

        val storePlayer = World.getInstance().getPlayer(_storePlayerId) ?: return

        if (!player.isInsideRadius(storePlayer, Npc.INTERACTION_DISTANCE, true, false))
            return

        if (!(storePlayer.storeType == Player.StoreType.SELL || storePlayer.storeType == Player.StoreType.PACKAGE_SELL))
            return

        val storeList = storePlayer.sellList ?: return

        if (!player.accessLevel.allowTransaction()) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
            return
        }

        if (storePlayer.storeType == Player.StoreType.PACKAGE_SELL && storeList.items.size > _items!!.size)
            return

        if (!storeList.privateStoreBuy(player, _items))
            return

        if (storeList.items.isEmpty()) {
            storePlayer.storeType = Player.StoreType.NONE
            storePlayer.broadcastUserInfo()
        }
    }

    companion object {
        private val BATCH_LENGTH = 12 // length of one item
    }
}