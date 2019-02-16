package com.l2kt.gameserver.handler.itemhandlers

import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.handler.IItemHandler
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.instance.FeedableBeast
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.network.SystemMessageId

class BeastSpice : IItemHandler {
    override fun useItem(playable: Playable, item: ItemInstance, forceUse: Boolean) {
        if (playable !is Player)
            return

        if (playable.target !is FeedableBeast) {
            playable.sendPacket(SystemMessageId.INCORRECT_TARGET)
            return
        }

        var skillId = 0
        when (item.itemId) {
            6643 -> skillId = 2188
            6644 -> skillId = 2189
        }

        val skill = SkillTable.getInfo(skillId, 1)
        if (skill != null)
            playable.useMagic(skill, false, false)
    }
}