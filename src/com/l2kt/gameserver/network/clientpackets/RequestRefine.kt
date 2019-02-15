package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.xml.AugmentationData
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ExVariationResult
import com.l2kt.gameserver.network.serverpackets.InventoryUpdate
import com.l2kt.gameserver.network.serverpackets.StatusUpdate

/**
 * Format:(ch) dddd
 * @author -Wooden-
 */
class RequestRefine : AbstractRefinePacket() {
    private var _targetItemObjId: Int = 0
    private var _refinerItemObjId: Int = 0
    private var _gemStoneItemObjId: Int = 0
    private var _gemStoneCount: Int = 0

    override fun readImpl() {
        _targetItemObjId = readD()
        _refinerItemObjId = readD()
        _gemStoneItemObjId = readD()
        _gemStoneCount = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val targetItem = activeChar.inventory!!.getItemByObjectId(_targetItemObjId)
        if (targetItem == null) {
            activeChar.sendPacket(ExVariationResult(0, 0, 0))
            activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS)
            return
        }

        val refinerItem = activeChar.inventory!!.getItemByObjectId(_refinerItemObjId)
        if (refinerItem == null) {
            activeChar.sendPacket(ExVariationResult(0, 0, 0))
            activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS)
            return
        }

        val gemStoneItem = activeChar.inventory!!.getItemByObjectId(_gemStoneItemObjId)
        if (gemStoneItem == null) {
            activeChar.sendPacket(ExVariationResult(0, 0, 0))
            activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS)
            return
        }

        if (!AbstractRefinePacket.isValid(activeChar, targetItem, refinerItem, gemStoneItem)) {
            activeChar.sendPacket(ExVariationResult(0, 0, 0))
            activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS)
            return
        }

        val ls = AbstractRefinePacket.getLifeStone(refinerItem.itemId)
        if (ls == null) {
            activeChar.sendPacket(ExVariationResult(0, 0, 0))
            activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS)
            return
        }

        val lifeStoneLevel = ls.level
        val lifeStoneGrade = ls.grade
        if (_gemStoneCount != AbstractRefinePacket.getGemStoneCount(targetItem.item.crystalType)) {
            activeChar.sendPacket(ExVariationResult(0, 0, 0))
            activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS)
            return
        }

        // unequip item
        if (targetItem.isEquipped) {
            val unequipped = activeChar.inventory!!.unEquipItemInSlotAndRecord(targetItem.locationSlot)
            val iu = InventoryUpdate()

            for (itm in unequipped)
                iu.addModifiedItem(itm)

            activeChar.sendPacket(iu)
            activeChar.broadcastUserInfo()
        }

        // Consume the life stone
        if (!activeChar.destroyItem("RequestRefine", refinerItem, 1, null, false)) {
            activeChar.sendPacket(ExVariationResult(0, 0, 0))
            activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS)
            return
        }

        // Consume gemstones
        if (!activeChar.destroyItem("RequestRefine", gemStoneItem, _gemStoneCount, null, false)) {
            activeChar.sendPacket(ExVariationResult(0, 0, 0))
            activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS)
            return
        }

        val aug = AugmentationData.generateRandomAugmentation(lifeStoneLevel, lifeStoneGrade)
        targetItem.augmentation = aug

        val stat12 = 0x0000FFFF and aug.augmentationId
        val stat34 = aug.augmentationId shr 16
        activeChar.sendPacket(ExVariationResult(stat12, stat34, 1))

        val iu = InventoryUpdate()
        iu.addModifiedItem(targetItem)
        activeChar.sendPacket(iu)

        val su = StatusUpdate(activeChar)
        su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.currentLoad)
        activeChar.sendPacket(su)
    }
}