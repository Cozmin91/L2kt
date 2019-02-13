package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.item.type.CrystalType
import com.l2kt.gameserver.model.item.type.CrystalType.NONE
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.InventoryUpdate
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestCrystallizeItem : L2GameClientPacket() {
    private var _objectId: Int = 0
    private var _count: Int = 0

    override fun readImpl() {
        _objectId = readD()
        _count = readD()
    }

    override fun runImpl() {
        // Sanity check.
        if (_count <= 0)
            return

        // Client must be attached to a player instance.
        val player = client.activeChar ?: return

        // Player mustn't be already crystallizing or in store mode.
        if (player.isInStoreMode || player.isCrystallizing) {
            player.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE)
            return
        }

        // Player must own Crystallize skill.
        val skillLevel = player.getSkillLevel(L2Skill.SKILL_CRYSTALLIZE)
        if (skillLevel <= 0) {
            player.sendPacket(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW)
            return
        }

        // Item must exist on player inventory. It must be a crystallizable item.
        val item = player.inventory!!.getItemByObjectId(_objectId)
        if (item == null || item.isHeroItem || item.isShadowItem)
            return

        if (!item.item.isCrystallizable || item.item.crystalCount <= 0 || item.item.crystalType == NONE)
            return

        // Sanity check for count.
        _count = Math.min(_count, item.count)

        // Check if the player can crystallize items and return if false.
        var canCrystallize = true

        when (item.item.crystalType) {
            CrystalType.C -> if (skillLevel <= 1)
                canCrystallize = false

            CrystalType.B -> if (skillLevel <= 2)
                canCrystallize = false

            CrystalType.A -> if (skillLevel <= 3)
                canCrystallize = false

            CrystalType.S -> if (skillLevel <= 4)
                canCrystallize = false
            else -> canCrystallize = false
        }

        if (!canCrystallize) {
            player.sendPacket(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW)
            player.sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        player.isCrystallizing = true

        // Unequip the item if needed.
        if (item.isEquipped) {
            val iu = InventoryUpdate()
            for (items in player.inventory!!.unEquipItemInSlotAndRecord(item.locationSlot))
                iu.addModifiedItem(items)

            player.sendPacket(iu)

            val msg: SystemMessage
            msg = if (item.enchantLevel > 0)
                SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED)
                    .addNumber(item.enchantLevel).addItemName(item.itemId)
            else
                SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(item.itemId)

            player.sendPacket(msg)
        }

        // Remove the item from inventory.
        val removedItem = player.inventory!!.destroyItem("Crystalize", _objectId, _count, player, null)

        val iu = InventoryUpdate()
        iu.addRemovedItem(removedItem)
        player.sendPacket(iu)

        player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CRYSTALLIZED).addItemName(removedItem!!.itemId))

        // add crystals
        val crystals =
            player.inventory!!.addItem("Crystalize", item.item.crystalItemId, item.crystalCount, player, player)
        player.sendPacket(
            SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(crystals!!.itemId).addItemNumber(
                item.crystalCount
            )
        )

        player.broadcastUserInfo()
        player.isCrystallizing = false
    }
}