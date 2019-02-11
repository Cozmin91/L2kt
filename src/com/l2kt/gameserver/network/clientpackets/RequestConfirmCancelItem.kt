package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.item.type.CrystalType.*
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ExConfirmCancelItem

class RequestConfirmCancelItem : L2GameClientPacket() {
    private var _objectId: Int = 0

    override fun readImpl() {
        _objectId = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val item = activeChar.inventory!!.getItemByObjectId(_objectId) ?: return

        if (item.ownerId != activeChar.objectId)
            return

        if (!item.isAugmented) {
            activeChar.sendPacket(SystemMessageId.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM)
            return
        }

        val price: Int
        when (item.item.crystalType) {
            C -> price = when {
                item.crystalCount < 1720 -> 95000
                item.crystalCount < 2452 -> 150000
                else -> 210000
            }

            B -> price = if (item.crystalCount < 1746)
                240000
            else
                270000

            A -> price = when {
                item.crystalCount < 2160 -> 330000
                item.crystalCount < 2824 -> 390000
                else -> 420000
            }

            S -> price = 480000
            // any other item type is not augmentable
            else -> return
        }

        activeChar.sendPacket(ExConfirmCancelItem(item, price))
    }
}