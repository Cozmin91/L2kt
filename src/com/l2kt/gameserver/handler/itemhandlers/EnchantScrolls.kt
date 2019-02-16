package com.l2kt.gameserver.handler.itemhandlers

import com.l2kt.gameserver.handler.IItemHandler
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ChooseInventoryItem

class EnchantScrolls : IItemHandler {
    override fun useItem(playable: Playable, item: ItemInstance, forceUse: Boolean) {
        if (playable !is Player)
            return

        if (playable.isCastingNow)
            return

        if (playable.activeEnchantItem == null)
            playable.sendPacket(SystemMessageId.SELECT_ITEM_TO_ENCHANT)

        playable.activeEnchantItem = item
        playable.sendPacket(ChooseInventoryItem(item.itemId))
    }
}
