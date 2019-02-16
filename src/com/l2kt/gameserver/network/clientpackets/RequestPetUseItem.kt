package com.l2kt.gameserver.network.clientpackets

import com.l2kt.commons.util.ArraysUtil
import com.l2kt.gameserver.handler.ItemHandler
import com.l2kt.gameserver.model.actor.instance.Pet
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.PetItemList
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestPetUseItem : L2GameClientPacket() {

    private var _objectId: Int = 0

    override fun readImpl() {
        _objectId = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar
        if (activeChar == null || !activeChar.hasPet())
            return

        val pet = activeChar.pet as Pet

        val item = pet.inventory!!.getItemByObjectId(_objectId) ?: return

        if (activeChar.isAlikeDead || pet.isDead) {
            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(item))
            return
        }

        if (!item.isEquipped && !item.item.checkCondition(pet, pet, true))
            return

        // Check if item is pet armor or pet weapon
        if (item.isPetItem) {
            // Verify if the pet can wear that item
            if (!pet.canWear(item.item)) {
                activeChar.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM)
                return
            }

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

        if (ArraysUtil.contains(PET_FOOD_IDS, item.itemId) && !pet.template.canEatFood(item.itemId)) {
            activeChar.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM)
            return
        }

        // If pet food check is successful or if the item got an handler, use that item.
        val handler = ItemHandler.getHandler(item.etcItem)
        if (handler != null) {
            handler.useItem(pet, item, false)
            pet.updateAndBroadcastStatus(1)
        } else
            activeChar.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM)

        return
    }

    companion object {
        private val PET_FOOD_IDS = intArrayOf(2515, 4038, 5168, 5169, 6316, 7582)
    }
}