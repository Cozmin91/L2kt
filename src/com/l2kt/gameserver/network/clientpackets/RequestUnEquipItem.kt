package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.item.kind.Item
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.InventoryUpdate
import com.l2kt.gameserver.network.serverpackets.SystemMessage

/**
 * format: cd
 */
class RequestUnEquipItem : L2GameClientPacket() {
    private var _slot: Int = 0

    override fun readImpl() {
        _slot = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val item = activeChar.inventory!!.getPaperdollItemByL2ItemId(_slot) ?: return

        // Prevent of unequiping a cursed weapon
        if (_slot == Item.SLOT_LR_HAND && activeChar.isCursedWeaponEquipped)
            return

        // Prevent player from unequipping items in special conditions
        if (activeChar.isStunned || activeChar.isSleeping || activeChar.isParalyzed || activeChar.isAfraid || activeChar.isAlikeDead) {
            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(item))
            return
        }

        if (activeChar.isCastingNow || activeChar.isCastingSimultaneouslyNow)
            return

        val unequipped = activeChar.inventory!!.unEquipItemInBodySlotAndRecord(_slot)

        // show the update in the inventory
        val iu = InventoryUpdate()
        for (itm in unequipped) {
            itm.unChargeAllShots()
            iu.addModifiedItem(itm)
        }
        activeChar.sendPacket(iu)
        activeChar.broadcastUserInfo()

        // this can be 0 if the user pressed the right mousebutton twice very fast
        if (unequipped.isNotEmpty()) {
            val sm: SystemMessage
            if (unequipped[0].enchantLevel > 0) {
                sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED)
                sm.addNumber(unequipped[0].enchantLevel)
                sm.addItemName(unequipped[0])
            } else {
                sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED)
                sm.addItemName(unequipped[0])
            }
            activeChar.sendPacket(sm)
        }
    }
}