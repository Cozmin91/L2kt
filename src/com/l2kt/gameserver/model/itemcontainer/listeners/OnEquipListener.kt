package com.l2kt.gameserver.model.itemcontainer.listeners

import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.item.instance.ItemInstance

interface OnEquipListener {
    fun onEquip(slot: Int, item: ItemInstance, actor: Playable)

    fun onUnequip(slot: Int, item: ItemInstance, actor: Playable)
}