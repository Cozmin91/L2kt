package com.l2kt.gameserver.model.itemcontainer.listeners

import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.xml.ArmorSetData
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.itemcontainer.Inventory

object ArmorSetListener : OnEquipListener {

    override fun onEquip(slot: Int, item: ItemInstance, actor: Playable) {
        if (!item.isEquipable)
            return

        val player = actor as Player

        // Checks if player is wearing a chest item
        val chestItem = player.inventory!!.getPaperdollItem(Inventory.PAPERDOLL_CHEST) ?: return

        // checks if there is armorset for chest item that player worns
        val armorSet = ArmorSetData.getSet(chestItem.itemId) ?: return

        // checks if equipped item is part of set
        if (armorSet.containItem(slot, item.itemId)) {
            if (armorSet.containAll(player)) {
                val skill = SkillTable.getInfo(armorSet.skillId, 1)
                if (skill != null) {
                    player.addSkill(SkillTable.getInfo(3006, 1), false)
                    player.addSkill(skill, false)
                    player.sendSkillList()
                }

                if (armorSet.containShield(player))
                // has shield from set
                {
                    val skills = SkillTable.getInfo(armorSet.shieldSkillId, 1)
                    if (skills != null) {
                        player.addSkill(skills, false)
                        player.sendSkillList()
                    }
                }

                if (armorSet.isEnchanted6(player))
                // has all parts of set enchanted to 6 or more
                {
                    val skillId = armorSet.enchant6skillId
                    if (skillId > 0) {
                        val skille = SkillTable.getInfo(skillId, 1)
                        if (skille != null) {
                            player.addSkill(skille, false)
                            player.sendSkillList()
                        }
                    }
                }
            }
        } else if (armorSet.containShield(item.itemId)) {
            if (armorSet.containAll(player)) {
                val skills = SkillTable.getInfo(armorSet.shieldSkillId, 1)
                if (skills != null) {
                    player.addSkill(skills, false)
                    player.sendSkillList()
                }
            }
        }
    }

    override fun onUnequip(slot: Int, item: ItemInstance, actor: Playable) {
        val player = actor as Player

        var remove = false
        var removeSkillId1 = 0 // set skill
        var removeSkillId2 = 0 // shield skill
        var removeSkillId3 = 0 // enchant +6 skill

        if (slot == Inventory.PAPERDOLL_CHEST) {
            val armorSet = ArmorSetData.getSet(item.itemId) ?: return

            remove = true
            removeSkillId1 = armorSet.skillId
            removeSkillId2 = armorSet.shieldSkillId
            removeSkillId3 = armorSet.enchant6skillId
        } else {
            val chestItem = player.inventory!!.getPaperdollItem(Inventory.PAPERDOLL_CHEST) ?: return

            val armorSet = ArmorSetData.getSet(chestItem.itemId) ?: return

            if (armorSet.containItem(slot, item.itemId))
            // removed part of set
            {
                remove = true
                removeSkillId1 = armorSet.skillId
                removeSkillId2 = armorSet.shieldSkillId
                removeSkillId3 = armorSet.enchant6skillId
            } else if (armorSet.containShield(item.itemId))
            // removed shield
            {
                remove = true
                removeSkillId2 = armorSet.shieldSkillId
            }
        }

        if (remove) {
            if (removeSkillId1 != 0) {
                player.removeSkill(3006, false)
                player.removeSkill(removeSkillId1, false)
            }

            if (removeSkillId2 != 0)
                player.removeSkill(removeSkillId2, false)

            if (removeSkillId3 != 0)
                player.removeSkill(removeSkillId3, false)

            player.sendSkillList()
        }
    }
}