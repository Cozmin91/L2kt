package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ExConfirmVariationGemstone

/**
 * Format:(ch) dddd
 * @author -Wooden-
 */
class RequestConfirmGemStone : AbstractRefinePacket() {
    private var _targetItemObjId: Int = 0
    private var _refinerItemObjId: Int = 0
    private var _gemstoneItemObjId: Int = 0
    private var _gemStoneCount: Int = 0

    override fun readImpl() {
        _targetItemObjId = readD()
        _refinerItemObjId = readD()
        _gemstoneItemObjId = readD()
        _gemStoneCount = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val targetItem = activeChar.inventory!!.getItemByObjectId(_targetItemObjId) ?: return

        val refinerItem = activeChar.inventory!!.getItemByObjectId(_refinerItemObjId) ?: return

        val gemStoneItem = activeChar.inventory!!.getItemByObjectId(_gemstoneItemObjId) ?: return

        // Make sure the item is a gemstone
        if (!AbstractRefinePacket.isValid(activeChar, targetItem, refinerItem, gemStoneItem)) {
            activeChar.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM)
            return
        }

        // Check for gemstone count
        AbstractRefinePacket.getLifeStone(refinerItem.itemId) ?: return

        if (_gemStoneCount != AbstractRefinePacket.getGemStoneCount(targetItem.item.crystalType)) {
            activeChar.sendPacket(SystemMessageId.GEMSTONE_QUANTITY_IS_INCORRECT)
            return
        }

        activeChar.sendPacket(ExConfirmVariationGemstone(_gemstoneItemObjId, _gemStoneCount))
    }
}