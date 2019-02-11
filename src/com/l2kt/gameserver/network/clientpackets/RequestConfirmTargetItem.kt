package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ExConfirmVariationItem

/**
 * Format:(ch) d
 * @author -Wooden-
 */
class RequestConfirmTargetItem : AbstractRefinePacket() {
    private var _itemObjId: Int = 0

    override fun readImpl() {
        _itemObjId = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val item = activeChar.inventory!!.getItemByObjectId(_itemObjId) ?: return

        if (!AbstractRefinePacket.Companion.isValid(activeChar, item)) {
            if (item.isAugmented) {
                activeChar.sendPacket(SystemMessageId.ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN)
                return
            }

            activeChar.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM)
            return
        }

        activeChar.sendPacket(ExConfirmVariationItem(_itemObjId))
    }
}