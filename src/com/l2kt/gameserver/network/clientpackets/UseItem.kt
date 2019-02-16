package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.handler.ItemHandler
import com.l2kt.gameserver.model.actor.instance.Pet
import com.l2kt.gameserver.model.item.kind.Item
import com.l2kt.gameserver.model.item.type.ActionType
import com.l2kt.gameserver.model.item.type.EtcItemType
import com.l2kt.gameserver.model.item.type.WeaponType
import com.l2kt.gameserver.model.itemcontainer.Inventory
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ItemList
import com.l2kt.gameserver.network.serverpackets.PetItemList
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.templates.skills.L2SkillType

class UseItem : L2GameClientPacket() {
    private var _objectId: Int = 0
    private var _ctrlPressed: Boolean = false

    override fun readImpl() {
        _objectId = readD()
        _ctrlPressed = readD() != 0
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        if (activeChar.isInStoreMode) {
            activeChar.sendPacket(SystemMessageId.ITEMS_UNAVAILABLE_FOR_STORE_MANUFACTURE)
            return
        }

        if (activeChar.activeTradeList != null) {
            activeChar.sendPacket(SystemMessageId.CANNOT_PICKUP_OR_USE_ITEM_WHILE_TRADING)
            return
        }

        val item = activeChar.inventory!!.getItemByObjectId(_objectId) ?: return

        if (item.item.type2 == Item.TYPE2_QUEST) {
            activeChar.sendPacket(SystemMessageId.CANNOT_USE_QUEST_ITEMS)
            return
        }

        if (activeChar.isAlikeDead || activeChar.isStunned || activeChar.isSleeping || activeChar.isParalyzed || activeChar.isAfraid)
            return

        if (!Config.KARMA_PLAYER_CAN_TELEPORT && activeChar.karma > 0) {
            val sHolders = item.item.skills
            if (sHolders != null) {
                for (sHolder in sHolders) {
                    val skill = sHolder.skill
                    if (skill != null && (skill.skillType === L2SkillType.TELEPORT || skill.skillType === L2SkillType.RECALL))
                        return
                }
            }
        }

        if (activeChar.isFishing && item.item.defaultAction != ActionType.fishingshot) {
            activeChar.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_3)
            return
        }

        /*
		 * The player can't use pet items if no pet is currently summoned. If a pet is summoned and player uses the item directly, it will be used by the pet.
		 */
        if (item.isPetItem) {
            // If no pet, cancels the use
            if (!activeChar.hasPet()) {
                activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_PET_ITEM)
                return
            }

            val pet = activeChar.pet as Pet

            if (!pet.canWear(item.item)) {
                activeChar.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM)
                return
            }

            if (pet.isDead) {
                activeChar.sendPacket(SystemMessageId.CANNOT_GIVE_ITEMS_TO_DEAD_PET)
                return
            }

            if (!pet.inventory!!.validateCapacity(item)) {
                activeChar.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS)
                return
            }

            if (!pet.inventory!!.validateWeight(item, 1)) {
                activeChar.sendPacket(SystemMessageId.UNABLE_TO_PLACE_ITEM_YOUR_PET_IS_TOO_ENCUMBERED)
                return
            }

            activeChar.transferItem("Transfer", _objectId, 1, pet.inventory, pet)

            // Equip it, removing first the previous item.
            if (item.isEquipped) {
                pet.inventory!!.unEquipItemInSlot(item.locationSlot)
                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_OFF_S1).addItemName(item))
            } else {
                pet.inventory!!.equipPetItem(item)
                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_PUT_ON_S1).addItemName(item))
            }

            activeChar.sendPacket(PetItemList(pet))
            pet.updateAndBroadcastStatus(1)
            return
        }

        if (!item.isEquipped) {
            if (!item.item.checkCondition(activeChar, activeChar, true))
                return
        }

        if (item.isEquipable) {
            if (activeChar.isCastingNow || activeChar.isCastingSimultaneouslyNow) {
                activeChar.sendPacket(SystemMessageId.CANNOT_USE_ITEM_WHILE_USING_MAGIC)
                return
            }

            when (item.item.bodyPart) {
                Item.SLOT_LR_HAND, Item.SLOT_L_HAND, Item.SLOT_R_HAND -> {
                    if (activeChar.isMounted) {
                        activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION)
                        return
                    }

                    // Don't allow weapon/shield equipment if a cursed weapon is equipped
                    if (activeChar.isCursedWeaponEquipped)
                        return
                }
            }

            if (activeChar.isCursedWeaponEquipped && item.itemId == 6408)
            // Don't allow to put formal wear
                return

            if (activeChar.isAttackingNow)
                ThreadPool.schedule(Runnable{
                    val itemToTest = activeChar.inventory!!.getItemByObjectId(_objectId) ?: return@Runnable

                    activeChar.useEquippableItem(itemToTest, false)
                }, activeChar.attackEndTime - System.currentTimeMillis())
            else
                activeChar.useEquippableItem(item, true)
        } else {
            if (activeChar.isCastingNow && !(item.isPotion || item.isElixir))
                return

            if (activeChar.attackType == WeaponType.FISHINGROD && item.item.itemType === EtcItemType.LURE) {
                activeChar.inventory!!.setPaperdollItem(Inventory.PAPERDOLL_LHAND, item)
                activeChar.broadcastUserInfo()

                sendPacket(ItemList(activeChar, false))
                return
            }

            val handler = ItemHandler.getHandler(item.etcItem)
            handler?.useItem(activeChar, item, _ctrlPressed)

            for (quest in item.questEvents) {
                val state = activeChar.getQuestState(quest.name)
                if (state == null || !state.isStarted)
                    continue

                quest.notifyItemUse(item, activeChar, activeChar.target)
            }
        }
    }
}