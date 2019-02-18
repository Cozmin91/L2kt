package com.l2kt.gameserver.handler.itemhandlers

import com.l2kt.gameserver.extensions.toSelfAndKnownPlayersInRadius
import com.l2kt.gameserver.handler.IItemHandler
import com.l2kt.gameserver.model.ShotType
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.Summon
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.MagicSkillUse
import com.l2kt.gameserver.network.serverpackets.SystemMessage

/**
 * Beast SpiritShot Handler
 * @author Tempy
 */
class BeastSpiritShot : IItemHandler {
    override fun useItem(playable: Playable, item: ItemInstance, forceUse: Boolean) {
        val activeOwner = playable.actingPlayer ?: return

        if (playable is Summon) {
            activeOwner.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM)
            return
        }

        val activePet = activeOwner.pet
        if (activePet == null) {
            activeOwner.sendPacket(SystemMessageId.PETS_ARE_NOT_AVAILABLE_AT_THIS_TIME)
            return
        }

        if (activePet.isDead()) {
            activeOwner.sendPacket(SystemMessageId.SOULSHOTS_AND_SPIRITSHOTS_ARE_NOT_AVAILABLE_FOR_A_DEAD_PET)
            return
        }

        val itemId = item.itemId
        val isBlessed = itemId == 6647

        // shots are already active.
        if (activePet.isChargedShot(if (isBlessed) ShotType.BLESSED_SPIRITSHOT else ShotType.SPIRITSHOT))
            return

        if (!activeOwner.destroyItemWithoutTrace("Consume", item.objectId, activePet.spiritShotsPerHit, null, false)) {
            if (!activeOwner.disableAutoShot(itemId))
                activeOwner.sendPacket(SystemMessageId.NOT_ENOUGH_SPIRITSHOTS_FOR_PET)
            return
        }

        activeOwner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_USES_S1).addItemName(itemId))
        activePet.setChargedShot(if (isBlessed) ShotType.BLESSED_SPIRITSHOT else ShotType.SPIRITSHOT, true)
        activeOwner.toSelfAndKnownPlayersInRadius(
            MagicSkillUse(
                activePet,
                activePet,
                if (isBlessed) 2009 else 2008,
                1,
                0,
                0
            ), 600
        )
    }
}