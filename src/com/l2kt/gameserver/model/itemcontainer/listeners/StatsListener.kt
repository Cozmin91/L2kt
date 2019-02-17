package com.l2kt.gameserver.model.itemcontainer.listeners

import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.item.instance.ItemInstance

object StatsListener : OnEquipListener {

    override fun onEquip(slot: Int, item: ItemInstance, playable: Playable) {
        playable.addStatFuncs(item.getStatFuncs(playable))
    }

    override fun onUnequip(slot: Int, item: ItemInstance, playable: Playable) {
        playable.removeStatsByOwner(item)
    }
}