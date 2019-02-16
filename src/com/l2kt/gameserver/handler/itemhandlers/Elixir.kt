package com.l2kt.gameserver.handler.itemhandlers

import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class Elixir : ItemSkills() {
    override fun useItem(playable: Playable, item: ItemInstance, forceUse: Boolean) {
        if (playable !is Player) {
            playable.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ITEM_NOT_FOR_PETS))
            return
        }
        super.useItem(playable, item, forceUse)
    }
}