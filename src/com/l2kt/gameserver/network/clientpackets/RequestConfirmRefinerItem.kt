package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ExConfirmVariationRefiner

/**
 * Fromat(ch) dd
 * @author -Wooden-
 */
class RequestConfirmRefinerItem : AbstractRefinePacket() {
    private var _targetItemObjId: Int = 0
    private var _refinerItemObjId: Int = 0

    override fun readImpl() {
        _targetItemObjId = readD()
        _refinerItemObjId = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val targetItem = activeChar.inventory!!.getItemByObjectId(_targetItemObjId) ?: return

        val refinerItem = activeChar.inventory!!.getItemByObjectId(_refinerItemObjId) ?: return

        if (!AbstractRefinePacket.isValid(activeChar, targetItem, refinerItem)) {
            activeChar.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM)
            return
        }

        val refinerItemId = refinerItem.item.itemId
        val grade = targetItem.item.crystalType
        val gemStoneId = AbstractRefinePacket.getGemStoneId(grade)
        val gemStoneCount = AbstractRefinePacket.getGemStoneCount(grade)

        activeChar.sendPacket(ExConfirmVariationRefiner(_refinerItemObjId, refinerItemId, gemStoneId, gemStoneCount))
    }
}