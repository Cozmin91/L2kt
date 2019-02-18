package com.l2kt.gameserver.handler.itemhandlers

import com.l2kt.Config
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.handler.IItemHandler
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.instance.Monster
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.network.SystemMessageId

class Harvester : IItemHandler {
    override fun useItem(playable: Playable, item: ItemInstance, forceUse: Boolean) {
        if (playable !is Player)
            return

        if (!Config.ALLOW_MANOR)
            return

        if (playable.target !is Monster) {
            playable.sendPacket(SystemMessageId.INCORRECT_TARGET)
            return
        }

        val _target = playable.target as Monster
        if (!_target.isDead()) {
            playable.sendPacket(SystemMessageId.INCORRECT_TARGET)
            return
        }

        val skill = SkillTable.getInfo(2098, 1)
        if (skill != null)
            playable.useMagic(skill, false, false)
    }
}