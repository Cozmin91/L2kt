package com.l2kt.gameserver.handler.itemhandlers

import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.handler.IItemHandler
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.instance.Pet
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.network.SystemMessageId

class ScrollOfResurrection : IItemHandler {
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

        // Target must be a dead pet or player.
        if (target !is Pet && target !is Player || !target.isDead()) {
            playable.sendPacket(SystemMessageId.INCORRECT_TARGET)
            return
        }

        // Pet scrolls to ress a player.
        if (item.itemId == 6387 && target is Player) {
            playable.sendPacket(SystemMessageId.INCORRECT_TARGET)
            return
        }

        // Pickup player, or pet owner in case target is a pet.
        val targetPlayer = target.actingPlayer

        // Check if target isn't in a active siege zone.
        val siege = CastleManager.getActiveSiege(targetPlayer!!)
        if (siege != null) {
            playable.sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE)
            return
        }

        // Check if the target is in a festival.
        if (targetPlayer.isFestivalParticipant)
            return

        if (targetPlayer.isReviveRequested) {
            if (targetPlayer.isRevivingPet)
                playable.sendPacket(SystemMessageId.MASTER_CANNOT_RES) // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
            else
                playable.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED) // Resurrection is already been proposed.

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

            // Scroll consumption is made on skill call, not on item call.
            playable.useMagic(itemSkill, false, false)
        }
    }
}