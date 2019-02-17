package com.l2kt.gameserver.model.itemcontainer.listeners

import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.item.type.WeaponType
import com.l2kt.gameserver.model.itemcontainer.Inventory

object BowRodListener : OnEquipListener {

    override fun onEquip(slot: Int, item: ItemInstance, actor: Playable) {
        if (slot != Inventory.PAPERDOLL_RHAND)
            return

        if (item.itemType === WeaponType.BOW) {
            val arrow = actor.inventory!!.findArrowForBow(item.item)
            if (arrow != null)
                actor.inventory!!.setPaperdollItem(Inventory.PAPERDOLL_LHAND, arrow)
        }
    }

    override fun onUnequip(slot: Int, item: ItemInstance, actor: Playable) {
        if (slot != Inventory.PAPERDOLL_RHAND)
            return

        if (item.itemType === WeaponType.BOW) {
            val arrow = actor.inventory!!.getPaperdollItem(Inventory.PAPERDOLL_LHAND)
            if (arrow != null)
                actor.inventory!!.setPaperdollItem(Inventory.PAPERDOLL_LHAND, null)
        } else if (item.itemType === WeaponType.FISHINGROD) {
            val lure = actor.inventory!!.getPaperdollItem(Inventory.PAPERDOLL_LHAND)
            if (lure != null)
                actor.inventory!!.setPaperdollItem(Inventory.PAPERDOLL_LHAND, null)
        }
    }
}