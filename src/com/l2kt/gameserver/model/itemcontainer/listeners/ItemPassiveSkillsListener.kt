package com.l2kt.gameserver.model.itemcontainer.listeners

import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.item.kind.Weapon
import com.l2kt.gameserver.network.serverpackets.SkillCoolTime

object ItemPassiveSkillsListener : OnEquipListener {

    override fun onEquip(slot: Int, item: ItemInstance, actor: Playable) {
        val player = actor as Player
        val it = item.item

        var update = false
        var updateTimeStamp = false

        if (it is Weapon) {
            // Apply augmentation bonuses on equip
            if (item.isAugmented)
                item.getAugmentation()!!.applyBonus(player)

            // Verify if the grade penalty is occuring. If yes, then forget +4 dual skills and SA attached to weapon.
            if (player.getSkillLevel(L2Skill.SKILL_EXPERTISE) < it.crystalType!!.id)
                return

            // Add skills bestowed from +4 Duals
            if (item.enchantLevel >= 4) {
                val enchant4Skill = it.enchant4Skill
                if (enchant4Skill != null) {
                    player.addSkill(enchant4Skill, false)
                    update = true
                }
            }
        }

        val skills = it.skills.toTypedArray()
        for (skillInfo in skills) {

            val itemSkill = skillInfo.skill
            if (itemSkill != null) {
                player.addSkill(itemSkill, false)

                if (itemSkill.isActive) {
                    if (!player.reuseTimeStamp.containsKey(itemSkill.reuseHashCode)) {
                        val equipDelay = itemSkill.equipDelay
                        if (equipDelay > 0) {
                            player.addTimeStamp(itemSkill, equipDelay.toLong())
                            player.disableSkill(itemSkill, equipDelay.toLong())
                        }
                    }
                    updateTimeStamp = true
                }
                update = true
            }
        }

        if (update) {
            player.sendSkillList()

            if (updateTimeStamp)
                player.sendPacket(SkillCoolTime(player))
        }
    }

    override fun onUnequip(slot: Int, item: ItemInstance, actor: Playable) {
        val player = actor as Player
        val it = item.item

        var update = false

        if (it is Weapon) {
            // Remove augmentation bonuses on unequip
            if (item.isAugmented)
                item.getAugmentation()!!.removeBonus(player)

            // Remove skills bestowed from +4 Duals
            if (item.enchantLevel >= 4) {
                val enchant4Skill = it.enchant4Skill
                if (enchant4Skill != null) {
                    player.removeSkill(enchant4Skill.id, false, enchant4Skill.isPassive || enchant4Skill.isToggle)
                    update = true
                }
            }
        }

        val skills = it.skills.toTypedArray()
        for (skillInfo in skills) {

            val itemSkill = skillInfo.skill
            if (itemSkill != null) {
                var found = false

                for (pItem in player.inventory!!.paperdollItems) {
                    if (pItem != null && it.itemId == pItem.itemId) {
                        found = true
                        break
                    }
                }

                if (!found) {
                    player.removeSkill(itemSkill.id, false, itemSkill.isPassive || itemSkill.isToggle)
                    update = true
                }
            }
        }

        if (update)
            player.sendSkillList()
    }
}