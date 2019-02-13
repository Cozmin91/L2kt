package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.item.type.CrystalType
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ExVariationCancelResult
import com.l2kt.gameserver.network.serverpackets.InventoryUpdate
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestRefineCancel : L2GameClientPacket() {
    private var _targetItemObjId: Int = 0

    override fun readImpl() {
        _targetItemObjId = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val targetItem = activeChar.inventory!!.getItemByObjectId(_targetItemObjId)
        if (targetItem == null) {
            activeChar.sendPacket(ExVariationCancelResult(0))
            return
        }

        if (targetItem.ownerId != activeChar.objectId)
            return

        // cannot remove augmentation from a not augmented item
        if (!targetItem.isAugmented) {
            activeChar.sendPacket(SystemMessageId.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM)
            activeChar.sendPacket(ExVariationCancelResult(0))
            return
        }

        // get the price
        var price: Int
        when (targetItem.item.crystalType) {
            CrystalType.C -> price = when {
                targetItem.crystalCount < 1720 -> 95000
                targetItem.crystalCount < 2452 -> 150000
                else -> 210000
            }

            CrystalType.B -> price = if (targetItem.crystalCount < 1746)
                240000
            else
                270000

            CrystalType.A -> price = when {
                targetItem.crystalCount < 2160 -> 330000
                targetItem.crystalCount < 2824 -> 390000
                else -> 420000
            }

            CrystalType.S -> price = 480000

            // any other item type is not augmentable
            else -> {
                activeChar.sendPacket(ExVariationCancelResult(0))
                return
            }
        }

        // try to reduce the players adena
        if (!activeChar.reduceAdena("RequestRefineCancel", price, null, true)) {
            activeChar.sendPacket(ExVariationCancelResult(0))
            return
        }

        // unequip item
        if (targetItem.isEquipped)
            activeChar.disarmWeapons()

        // remove the augmentation
        targetItem.removeAugmentation()

        // send ExVariationCancelResult
        activeChar.sendPacket(ExVariationCancelResult(1))

        // send inventory update
        val iu = InventoryUpdate()
        iu.addModifiedItem(targetItem)
        activeChar.sendPacket(iu)

        // send system message
        val sm = SystemMessage.getSystemMessage(SystemMessageId.AUGMENTATION_HAS_BEEN_SUCCESSFULLY_REMOVED_FROM_YOUR_S1)
        sm.addItemName(targetItem)
        activeChar.sendPacket(sm)
    }
}