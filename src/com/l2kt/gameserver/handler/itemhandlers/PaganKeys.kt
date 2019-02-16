package com.l2kt.gameserver.handler.itemhandlers

import com.l2kt.gameserver.data.xml.DoorData
import com.l2kt.gameserver.handler.IItemHandler
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.instance.Door
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ActionFailed

class PaganKeys : IItemHandler {
    override fun useItem(playable: Playable, item: ItemInstance, forceUse: Boolean) {
        if (playable !is Player)
            return

        val target = playable.target

        if (target !is Door) {
            playable.sendPacket(SystemMessageId.INCORRECT_TARGET)
            playable.sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        if (!playable.isInsideRadius(target, Npc.INTERACTION_DISTANCE, false, false)) {
            playable.sendPacket(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED)
            playable.sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        if (!playable.destroyItem("Consume", item.objectId, 1, null, true))
            return

        val doorId = target.doorId

        when (item.itemId) {
            8056 -> if (doorId == 23150004 || doorId == 23150003) {
                DoorData.getDoor(23150003)!!.openMe()
                DoorData.getDoor(23150004)!!.openMe()
            } else
                playable.sendPacket(SystemMessageId.TARGET_IS_INCORRECT)

            8273 -> when (doorId) {
                19160002, 19160003, 19160004, 19160005, 19160006, 19160007, 19160008, 19160009 -> DoorData.getDoor(
                    doorId
                )!!.openMe()

                else -> playable.sendPacket(SystemMessageId.TARGET_IS_INCORRECT)
            }

            8275 -> when (doorId) {
                19160012, 19160013 -> DoorData.getDoor(doorId)!!.openMe()

                else -> playable.sendPacket(SystemMessageId.TARGET_IS_INCORRECT)
            }
        }
    }
}