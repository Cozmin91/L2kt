package com.l2kt.gameserver.network.clientpackets

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.xml.ArmorSetData
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.item.kind.Armor
import com.l2kt.gameserver.model.item.kind.Weapon
import com.l2kt.gameserver.model.itemcontainer.Inventory
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.*

class RequestEnchantItem : AbstractEnchantPacket() {
    private var _objectId = 0

    override fun readImpl() {
        _objectId = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar
        if (activeChar == null || _objectId == 0)
            return

        if (!activeChar.isOnline || client.isDetached) {
            activeChar.activeEnchantItem = null
            return
        }

        if (activeChar.isProcessingTransaction || activeChar.isInStoreMode) {
            activeChar.sendPacket(SystemMessageId.CANNOT_ENCHANT_WHILE_STORE)
            activeChar.activeEnchantItem = null
            activeChar.sendPacket(EnchantResult.CANCELLED)
            return
        }

        val item = activeChar.inventory!!.getItemByObjectId(_objectId)
        var scroll: ItemInstance? = activeChar.activeEnchantItem

        if (item == null || scroll == null) {
            activeChar.activeEnchantItem = null
            activeChar.sendPacket(SystemMessageId.ENCHANT_SCROLL_CANCELLED)
            activeChar.sendPacket(EnchantResult.CANCELLED)
            return
        }

        // template for scroll
        val scrollTemplate = AbstractEnchantPacket.getEnchantScroll(scroll) ?: return

        // first validation check
        if (!scrollTemplate.isValid(item) || !AbstractEnchantPacket.isEnchantable(item)) {
            activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION)
            activeChar.activeEnchantItem = null
            activeChar.sendPacket(EnchantResult.CANCELLED)
            return
        }

        // attempting to destroy scroll
        scroll = activeChar.inventory!!.destroyItem("Enchant", scroll.objectId, 1, activeChar, item)
        if (scroll == null) {
            activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS)
            activeChar.activeEnchantItem = null
            activeChar.sendPacket(EnchantResult.CANCELLED)
            return
        }

        if (activeChar.activeTradeList != null) {
            activeChar.cancelActiveTrade()
            activeChar.sendPacket(SystemMessageId.TRADE_ATTEMPT_FAILED)
            return
        }

        synchronized(item) {
            val chance = scrollTemplate.getChance(item)

            // last validation check
            if (item.ownerId != activeChar.objectId || !AbstractEnchantPacket.isEnchantable(item) || chance < 0) {
                activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION)
                activeChar.activeEnchantItem = null
                activeChar.sendPacket(EnchantResult.CANCELLED)
                return
            }

            // success
            if (Rnd.nextDouble() < chance) {
                // announce the success
                val sm: SystemMessage

                if (item.enchantLevel == 0) {
                    sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SUCCESSFULLY_ENCHANTED)
                    sm.addItemName(item.itemId)
                    activeChar.sendPacket(sm)
                } else {
                    sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2_SUCCESSFULLY_ENCHANTED)
                    sm.addNumber(item.enchantLevel)
                    sm.addItemName(item.itemId)
                    activeChar.sendPacket(sm)
                }

                item.enchantLevel = item.enchantLevel + 1
                item.updateDatabase()

                // If item is equipped, verify the skill obtention (+4 duals, +6 armorset).
                if (item.isEquipped) {
                    val it = item.item

                    // Add skill bestowed by +4 duals.
                    if (it is Weapon && item.enchantLevel == 4) {
                        val enchant4Skill = it.enchant4Skill
                        if (enchant4Skill != null) {
                            activeChar.addSkill(enchant4Skill, false)
                            activeChar.sendSkillList()
                        }
                    } else if (it is Armor && item.enchantLevel == 6) {
                        // Checks if player is wearing a chest item
                        val chestItem = activeChar.inventory!!.getPaperdollItem(Inventory.PAPERDOLL_CHEST)
                        if (chestItem != null) {
                            val armorSet = ArmorSetData.getSet(chestItem.itemId)
                            if (armorSet != null && armorSet.isEnchanted6(activeChar))
                            // has all parts of set enchanted to 6 or more
                            {
                                val skillId = armorSet.enchant6skillId
                                if (skillId > 0) {
                                    val skill = SkillTable.getInfo(skillId, 1)
                                    if (skill != null) {
                                        activeChar.addSkill(skill, false)
                                        activeChar.sendSkillList()
                                    }
                                }
                            }
                        }
                    }// Add skill bestowed by +6 armorset.
                }
                activeChar.sendPacket(EnchantResult.SUCCESS)
            } else {
                // Drop passive skills from items.
                if (item.isEquipped) {
                    val it = item.item

                    // Remove skill bestowed by +4 duals.
                    if (it is Weapon && item.enchantLevel >= 4) {
                        val enchant4Skill = it.enchant4Skill
                        if (enchant4Skill != null) {
                            activeChar.removeSkill(enchant4Skill.id, false)
                            activeChar.sendSkillList()
                        }
                    } else if (it is Armor && item.enchantLevel >= 6) {
                        // Checks if player is wearing a chest item
                        val chestItem = activeChar.inventory!!.getPaperdollItem(Inventory.PAPERDOLL_CHEST)
                        if (chestItem != null) {
                            val armorSet = ArmorSetData.getSet(chestItem.itemId)
                            if (armorSet != null && armorSet.isEnchanted6(activeChar))
                            // has all parts of set enchanted to 6 or more
                            {
                                val skillId = armorSet.enchant6skillId
                                if (skillId > 0) {
                                    activeChar.removeSkill(skillId, false)
                                    activeChar.sendSkillList()
                                }
                            }
                        }
                    }// Add skill bestowed by +6 armorset.
                }

                if (scrollTemplate.isBlessed) {
                    // blessed enchant - clear enchant value
                    activeChar.sendPacket(SystemMessageId.BLESSED_ENCHANT_FAILED)

                    item.enchantLevel = 0
                    item.updateDatabase()
                    activeChar.sendPacket(EnchantResult.UNSUCCESS)
                } else {
                    // enchant failed, destroy item
                    val crystalId = item.item.crystalItemId
                    var count = item.crystalCount - (item.item.crystalCount + 1) / 2
                    if (count < 1)
                        count = 1

                    val destroyItem = activeChar.inventory!!.destroyItem("Enchant", item, activeChar, null)
                    if (destroyItem == null) {
                        activeChar.activeEnchantItem = null
                        activeChar.sendPacket(EnchantResult.CANCELLED)
                        return
                    }

                    if (crystalId != 0) {
                        activeChar.inventory!!.addItem("Enchant", crystalId, count, activeChar, destroyItem)
                        activeChar.sendPacket(
                            SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(
                                crystalId
                            ).addItemNumber(count)
                        )
                    }

                    val iu = InventoryUpdate()
                    if (destroyItem.count == 0)
                        iu.addRemovedItem(destroyItem)
                    else
                        iu.addModifiedItem(destroyItem)

                    activeChar.sendPacket(iu)

                    // Messages.
                    if (item.enchantLevel > 0)
                        activeChar.sendPacket(
                            SystemMessage.getSystemMessage(SystemMessageId.ENCHANTMENT_FAILED_S1_S2_EVAPORATED).addNumber(
                                item.enchantLevel
                            ).addItemName(item.itemId)
                        )
                    else
                        activeChar.sendPacket(
                            SystemMessage.getSystemMessage(SystemMessageId.ENCHANTMENT_FAILED_S1_EVAPORATED).addItemName(
                                item.itemId
                            )
                        )

                    World.removeObject(destroyItem)
                    if (crystalId == 0)
                        activeChar.sendPacket(EnchantResult.UNK_RESULT_4)
                    else
                        activeChar.sendPacket(EnchantResult.UNK_RESULT_1)

                    val su = StatusUpdate(activeChar)
                    su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.currentLoad)
                    activeChar.sendPacket(su)
                }
            }

            activeChar.sendPacket(ItemList(activeChar, false))
            activeChar.broadcastUserInfo()
            activeChar.activeEnchantItem = null
        }
    }
}