package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.xml.ArmorSetData
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.kind.Armor
import com.l2kt.gameserver.model.item.kind.Weapon
import com.l2kt.gameserver.model.itemcontainer.Inventory
import com.l2kt.gameserver.network.serverpackets.ItemList

/**
 * This class handles following admin commands: - enchant_armor
 */
class AdminEnchant : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (command == "admin_enchant")
            showMainPage(activeChar)
        else {
            var armorType = -1

            // check value

            // show the enchant menu after an action
            when {
                command.startsWith("admin_seteh") -> armorType = Inventory.PAPERDOLL_HEAD
                command.startsWith("admin_setec") -> armorType = Inventory.PAPERDOLL_CHEST
                command.startsWith("admin_seteg") -> armorType = Inventory.PAPERDOLL_GLOVES
                command.startsWith("admin_seteb") -> armorType = Inventory.PAPERDOLL_FEET
                command.startsWith("admin_setel") -> armorType = Inventory.PAPERDOLL_LEGS
                command.startsWith("admin_setew") -> armorType = Inventory.PAPERDOLL_RHAND
                command.startsWith("admin_setes") -> armorType = Inventory.PAPERDOLL_LHAND
                command.startsWith("admin_setle") -> armorType = Inventory.PAPERDOLL_LEAR
                command.startsWith("admin_setre") -> armorType = Inventory.PAPERDOLL_REAR
                command.startsWith("admin_setlf") -> armorType = Inventory.PAPERDOLL_LFINGER
                command.startsWith("admin_setrf") -> armorType = Inventory.PAPERDOLL_RFINGER
                command.startsWith("admin_seten") -> armorType = Inventory.PAPERDOLL_NECK
                command.startsWith("admin_setun") -> armorType = Inventory.PAPERDOLL_UNDER
                command.startsWith("admin_setba") -> armorType = Inventory.PAPERDOLL_BACK
            }

            if (armorType != -1) {
                try {
                    val ench = Integer.parseInt(command.substring(12))

                    // check value
                    if (ench < 0 || ench > 65535)
                        activeChar.sendMessage("You must set the enchant level to be between 0-65535.")
                    else
                        setEnchant(activeChar, ench, armorType)
                } catch (e: Exception) {
                    activeChar.sendMessage("Please specify a new enchant value.")
                }

            }

            // show the enchant menu after an action
            showMainPage(activeChar)
        }

        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf(
            "admin_seteh", // 6
            "admin_setec", // 10
            "admin_seteg", // 9
            "admin_setel", // 11
            "admin_seteb", // 12
            "admin_setew", // 7
            "admin_setes", // 8
            "admin_setle", // 1
            "admin_setre", // 2
            "admin_setlf", // 4
            "admin_setrf", // 5
            "admin_seten", // 3
            "admin_setun", // 0
            "admin_setba", // 13
            "admin_enchant"
        )

        private fun setEnchant(activeChar: Player, ench: Int, armorType: Int) {
            var target = activeChar.target
            if (target !is Player)
                target = activeChar

            val player = target

            val item = player.inventory!!.getPaperdollItem(armorType)
            if (item != null && item.locationSlot == armorType) {
                val it = item.item
                val oldEnchant = item.enchantLevel

                item.enchantLevel = ench
                item.updateDatabase()

                // If item is equipped, verify the skill obtention/drop (+4 duals, +6 armorset).
                if (item.isEquipped) {
                    val currentEnchant = item.enchantLevel

                    // Skill bestowed by +4 duals.
                    if (it is Weapon) {
                        // Old enchant was >= 4 and new is lower : we drop the skill.
                        if (oldEnchant >= 4 && currentEnchant < 4) {
                            val enchant4Skill = it.enchant4Skill
                            if (enchant4Skill != null) {
                                player.removeSkill(enchant4Skill.id, false)
                                player.sendSkillList()
                            }
                        } else if (oldEnchant < 4 && currentEnchant >= 4) {
                            val enchant4Skill = it.enchant4Skill
                            if (enchant4Skill != null) {
                                player.addSkill(enchant4Skill, false)
                                player.sendSkillList()
                            }
                        }// Old enchant was < 4 and new is 4 or more : we add the skill.
                    } else if (it is Armor) {
                        // Old enchant was >= 6 and new is lower : we drop the skill.
                        if (oldEnchant >= 6 && currentEnchant < 6) {
                            // Checks if player is wearing a chest item
                            val chestItem = player.inventory!!.getPaperdollItem(Inventory.PAPERDOLL_CHEST)
                            if (chestItem != null) {
                                val armorSet = ArmorSetData.getSet(chestItem.itemId)
                                if (armorSet != null) {
                                    val skillId = armorSet.enchant6skillId
                                    if (skillId > 0) {
                                        player.removeSkill(skillId, false)
                                        player.sendSkillList()
                                    }
                                }
                            }
                        } else if (oldEnchant < 6 && currentEnchant >= 6) {
                            // Checks if player is wearing a chest item
                            val chestItem = player.inventory!!.getPaperdollItem(Inventory.PAPERDOLL_CHEST)
                            if (chestItem != null) {
                                val armorSet = ArmorSetData.getSet(chestItem.itemId)
                                if (armorSet != null && armorSet.isEnchanted6(player))
                                // has all parts of set enchanted to 6 or more
                                {
                                    val skillId = armorSet.enchant6skillId
                                    if (skillId > 0) {
                                        val skill = SkillTable.getInfo(skillId, 1)
                                        if (skill != null) {
                                            player.addSkill(skill, false)
                                            player.sendSkillList()
                                        }
                                    }
                                }
                            }
                        }// Old enchant was < 6 and new is 6 or more : we add the skill.
                    }// Add skill bestowed by +6 armorset.
                }

                player.sendPacket(ItemList(player, false))
                player.broadcastUserInfo()

                activeChar.sendMessage("Changed enchantment of " + player.name + "'s " + it.name + " from " + oldEnchant + " to " + ench + ".")
                if (player != activeChar)
                    player.sendMessage("A GM has changed the enchantment of your " + it.name + " from " + oldEnchant + " to " + ench + ".")
            }
        }

        private fun showMainPage(activeChar: Player) {
            AdminHelpPage.showHelpPage(activeChar, "enchant.htm")
        }
    }
}