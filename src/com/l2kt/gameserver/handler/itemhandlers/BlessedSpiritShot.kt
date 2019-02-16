package com.l2kt.gameserver.handler.itemhandlers

import com.l2kt.gameserver.extensions.toSelfAndKnownPlayersInRadius
import com.l2kt.gameserver.handler.IItemHandler
import com.l2kt.gameserver.model.ShotType
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.MagicSkillUse

class BlessedSpiritShot : IItemHandler {
    override fun useItem(playable: Playable, item: ItemInstance, forceUse: Boolean) {
        if (playable !is Player)
            return

        val weaponInst = playable.activeWeaponInstance
        val weaponItem = playable.activeWeaponItem
        val itemId = item.itemId

        // Check if bss can be used
        if (weaponInst == null || weaponItem == null || weaponItem.spiritShotCount == 0) {
            if (!playable.autoSoulShot.contains(itemId))
                playable.sendPacket(SystemMessageId.CANNOT_USE_SPIRITSHOTS)
            return
        }

        // Check if bss is already active (it can be charged over SpiritShot)
        if (playable.isChargedShot(ShotType.BLESSED_SPIRITSHOT))
            return

        // Check for correct grade.
        if (weaponItem.crystalType != item.item.crystalType) {
            if (!playable.autoSoulShot.contains(itemId))
                playable.sendPacket(SystemMessageId.SPIRITSHOTS_GRADE_MISMATCH)

            return
        }

        // Consume bss if player has enough of them
        if (!playable.destroyItemWithoutTrace("Consume", item.objectId, weaponItem.spiritShotCount, null, false)) {
            if (!playable.disableAutoShot(itemId))
                playable.sendPacket(SystemMessageId.NOT_ENOUGH_SPIRITSHOTS)

            return
        }

        val skills = item.item.skills

        playable.sendPacket(SystemMessageId.ENABLED_SPIRITSHOT)
        playable.setChargedShot(ShotType.BLESSED_SPIRITSHOT, true)
        playable.toSelfAndKnownPlayersInRadius(MagicSkillUse(playable, playable, skills[0].id, 1, 0, 0), 600)
    }
}