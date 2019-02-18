package com.l2kt.gameserver.handler.itemhandlers

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.extensions.toSelfAndKnownPlayersInRadius
import com.l2kt.gameserver.handler.IItemHandler
import com.l2kt.gameserver.model.ShotType
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.MagicSkillUse

class SoulShots : IItemHandler {
    override fun useItem(playable: Playable, item: ItemInstance, forceUse: Boolean) {
        if (playable !is Player)
            return

        val weaponInst = playable.activeWeaponInstance
        val weaponItem = playable.activeWeaponItem
        val itemId = item.itemId

        // Check if soulshot can be used
        if (weaponInst == null || weaponItem!!.soulShotCount == 0) {
            if (!playable.autoSoulShot.contains(itemId))
                playable.sendPacket(SystemMessageId.CANNOT_USE_SOULSHOTS)
            return
        }

        if (weaponItem.crystalType != item.item.crystalType) {
            if (!playable.autoSoulShot.contains(itemId))
                playable.sendPacket(SystemMessageId.SOULSHOTS_GRADE_MISMATCH)

            return
        }

        // Check if Soulshot are already active.
        if (playable.isChargedShot(ShotType.SOULSHOT))
            return

        // Consume Soulshots if player has enough of them.
        var ssCount = weaponItem.soulShotCount
        if (weaponItem.reducedSoulShot > 0 && Rnd[100] < weaponItem.reducedSoulShotChance)
            ssCount = weaponItem.reducedSoulShot

        if (!playable.destroyItemWithoutTrace("Consume", item.objectId, ssCount, null, false)) {
            if (!playable.disableAutoShot(itemId))
                playable.sendPacket(SystemMessageId.NOT_ENOUGH_SOULSHOTS)

            return
        }

        val skills = item.item.skills

        weaponInst.setChargedShot(ShotType.SOULSHOT, true)
        playable.sendPacket(SystemMessageId.ENABLED_SOULSHOT)
        playable.toSelfAndKnownPlayersInRadius(MagicSkillUse(playable, playable, skills[0].id, 1, 0, 0), 600)
    }
}