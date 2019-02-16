package com.l2kt.gameserver.handler.itemhandlers

import com.l2kt.gameserver.handler.IItemHandler
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.instance.Chest
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.network.SystemMessageId

/**
 * That handler is used for the different types of keys. Such items aren't consumed until the skill is definitively launched.
 * @author Tryskell
 */
class Keys : IItemHandler {
    override fun useItem(playable: Playable, item: ItemInstance, forceUse: Boolean) {
        if (playable !is Player)
            return

        if (playable.isSitting) {
            playable.sendPacket(SystemMessageId.CANT_MOVE_SITTING)
            return
        }

        if (playable.isMovementDisabled)
            return

        val target = playable.target as Creature

        // Target must be a valid chest (not dead or already interacted).
        if (target !is Chest || target.isDead() || target.isInteracted) {
            playable.sendPacket(SystemMessageId.INCORRECT_TARGET)
            return
        }

        val skills = item.etcItem!!.skills
        if (skills == null) {
            IItemHandler.Companion.LOGGER.warn("{} doesn't have any registered skill for handler.", item.name)
            return
        }

        for (skillInfo in skills) {
            if (skillInfo == null)
                continue

            val itemSkill = skillInfo.skill ?: continue

            // Key consumption is made on skill call, not on item call.
            playable.useMagic(itemSkill, false, false)
        }
    }
}