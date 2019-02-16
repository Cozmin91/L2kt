package com.l2kt.gameserver.handler.itemhandlers

import com.l2kt.Config
import com.l2kt.gameserver.data.manager.CastleManorManager
import com.l2kt.gameserver.data.xml.MapRegionData
import com.l2kt.gameserver.handler.IItemHandler
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.network.SystemMessageId

class SeedHandler : IItemHandler {
    override fun useItem(playable: Playable, item: ItemInstance, forceUse: Boolean) {
        if (!Config.ALLOW_MANOR || playable !is Player)
            return

        val tgt = playable.getTarget()
        if (tgt !is Attackable || !tgt.template.isSeedable) {
            playable.sendPacket(SystemMessageId.THE_TARGET_IS_UNAVAILABLE_FOR_SEEDING)
            return
        }

        if (tgt.isDead || tgt.isSeeded) {
            playable.sendPacket(SystemMessageId.INCORRECT_TARGET)
            return
        }

        val seed = CastleManorManager.getSeed(item.itemId) ?: return

        if (seed.castleId != MapRegionData.getAreaCastle(playable.x, playable.y)) {
            playable.sendPacket(SystemMessageId.THIS_SEED_MAY_NOT_BE_SOWN_HERE)
            return
        }

        tgt.setSeeded(seed, playable.objectId)

        val skills = item.etcItem!!.skills
        if (skills != null) {
            if (skills[0] == null)
                return

            playable.useMagic(skills[0].skill, false, false)
        }
    }
}