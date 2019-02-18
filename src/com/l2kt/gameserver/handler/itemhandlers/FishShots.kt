package com.l2kt.gameserver.handler.itemhandlers

import com.l2kt.gameserver.extensions.toSelfAndKnownPlayers
import com.l2kt.gameserver.handler.IItemHandler
import com.l2kt.gameserver.model.ShotType
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.item.type.WeaponType
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.MagicSkillUse

/**
 * @author -Nemesiss-
 */
class FishShots : IItemHandler {
    override fun useItem(playable: Playable, item: ItemInstance, forceUse: Boolean) {
        if (playable !is Player)
            return

        val weaponInst = playable.activeWeaponInstance
        val weaponItem = playable.activeWeaponItem

        if (weaponInst == null || weaponItem!!.itemType != WeaponType.FISHINGROD)
            return

        // Fishshot is already active
        if (playable.isChargedShot(ShotType.FISH_SOULSHOT))
            return

        // Wrong grade of soulshot for that fishing pole.
        if (weaponItem.crystalType != item.item.crystalType) {
            playable.sendPacket(SystemMessageId.WRONG_FISHINGSHOT_GRADE)
            return
        }

        if (!playable.destroyItemWithoutTrace("Consume", item.objectId, 1, null, false)) {
            playable.sendPacket(SystemMessageId.NOT_ENOUGH_SOULSHOTS)
            return
        }

        val skills = item.item.skills

        playable.setChargedShot(ShotType.FISH_SOULSHOT, true)
        playable.toSelfAndKnownPlayers(MagicSkillUse(playable, skills[0].id, 1, 0, 0))
    }
}