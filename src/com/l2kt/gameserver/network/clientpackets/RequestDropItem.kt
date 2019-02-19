package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.model.item.kind.Item
import com.l2kt.gameserver.network.FloodProtectors
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.InventoryUpdate
import com.l2kt.gameserver.network.serverpackets.ItemList

class RequestDropItem : L2GameClientPacket() {
    private var _objectId: Int = 0
    private var _count: Int = 0
    private var _x: Int = 0
    private var _y: Int = 0
    private var _z: Int = 0

    override fun readImpl() {
        _objectId = readD()
        _count = readD()
        _x = readD()
        _y = readD()
        _z = readD()
    }

    override fun runImpl() {
        if (!FloodProtectors.performAction(client, FloodProtectors.Action.DROP_ITEM))
            return

        val activeChar = client.activeChar
        if (activeChar == null || activeChar.isDead())
            return

        val item = activeChar.validateItemManipulation(_objectId)
        if (item == null || _count == 0 || !Config.ALLOW_DISCARDITEM && !activeChar.isGM || !item.isDropable) {
            activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM)
            return
        }

        if (item.isQuestItem)
            return

        if (_count > item.count) {
            activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM)
            return
        }

        if (_count < 0)
            return

        if (!item.isStackable && _count > 1)
            return

        if (!activeChar.accessLevel.allowTransaction) {
            activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
            return
        }

        if (activeChar.isProcessingTransaction || activeChar.isInStoreMode) {
            activeChar.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE)
            return
        }

        if (activeChar.isFishing) {
            activeChar.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_2)
            return
        }

        if (item.isAugmented) {
            activeChar.sendPacket(SystemMessageId.AUGMENTED_ITEM_CANNOT_BE_DISCARDED)
            return
        }

        if (activeChar.isCastingNow) {
            if (activeChar.currentSkill.skill != null && activeChar.currentSkill.skill?.itemConsumeId == item.itemId) {
                activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM)
                return
            }
        }

        if (activeChar.isCastingSimultaneouslyNow) {
            if (activeChar.lastSimultaneousSkillCast != null && activeChar.lastSimultaneousSkillCast!!.itemConsumeId == item.itemId) {
                activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM)
                return
            }
        }

        if (Item.TYPE2_QUEST == item.item.type2 && !activeChar.isGM) {
            activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_EXCHANGE_ITEM)
            return
        }

        if (!activeChar.isInsideRadius(_x, _y, 150, false) || Math.abs(_z - activeChar.z) > 50) {
            activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_DISTANCE_TOO_FAR)
            return
        }

        if (item.isEquipped && (!item.isStackable || item.isStackable && _count >= item.count)) {
            val unequipped = activeChar.inventory!!.unEquipItemInBodySlotAndRecord(item)
            val iu = InventoryUpdate()
            for (itm in unequipped) {
                itm.unChargeAllShots()
                iu.addModifiedItem(itm)
            }

            activeChar.sendPacket(iu)
            activeChar.broadcastUserInfo()
            activeChar.sendPacket(ItemList(activeChar, true))
        }

        activeChar.dropItem("Drop", _objectId, _count, _x, _y, _z, null, false)
    }
}