package com.l2kt.gameserver.handler.itemhandlers

import com.l2kt.gameserver.handler.IItemHandler
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance

/**
 * Template for item skills handler.
 * @author Hasha
 */
class SoulCrystals : IItemHandler {
    override fun useItem(playable: Playable, item: ItemInstance, forceUse: Boolean) {
        if (playable !is Player)
            return

        val etcItem = item.etcItem

        val skills = etcItem!!.skills ?: return

        val itemSkill = skills[0].skill
        if (itemSkill == null || itemSkill.id != 2096)
            return

        if (playable.isCastingNow)
            return

        if (!itemSkill.checkCondition(playable, playable.target, false))
            return

        // No message on retail, the use is just forgotten.
        if (playable.isSkillDisabled(itemSkill))
            return

        playable.ai.setIntention(CtrlIntention.IDLE)
        if (!playable.useMagic(itemSkill, forceUse, false))
            return

        var reuseDelay = itemSkill.reuseDelay
        if (etcItem.reuseDelay > reuseDelay)
            reuseDelay = etcItem.reuseDelay

        playable.addTimeStamp(itemSkill, reuseDelay.toLong())
        if (reuseDelay != 0)
            playable.disableSkill(itemSkill, reuseDelay.toLong())
    }
}