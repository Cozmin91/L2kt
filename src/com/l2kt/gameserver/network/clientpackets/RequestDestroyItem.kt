package com.l2kt.gameserver.network.clientpackets

import com.l2kt.L2DatabaseFactory
import com.l2kt.gameserver.data.manager.CursedWeaponManager
import com.l2kt.gameserver.model.item.type.EtcItemType
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.InventoryUpdate

class RequestDestroyItem : L2GameClientPacket() {

    private var _objectId: Int = 0
    private var _count: Int = 0

    override fun readImpl() {
        _objectId = readD()
        _count = readD()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        if (player.isProcessingTransaction || player.isInStoreMode) {
            player.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE)
            return
        }

        val itemToRemove = player.inventory!!.getItemByObjectId(_objectId) ?: return

        if (_count < 1 || _count > itemToRemove.count) {
            player.sendPacket(SystemMessageId.CANNOT_DESTROY_NUMBER_INCORRECT)
            return
        }

        if (!itemToRemove.isStackable && _count > 1)
            return

        val itemId = itemToRemove.itemId

        // Cannot discard item that the skill is consumming
        if (player.isCastingNow && player.currentSkill.skill != null && player.currentSkill.skill.itemConsumeId == itemId) {
            player.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM)
            return
        }

        // Cannot discard item that the skill is consuming
        if (player.isCastingSimultaneouslyNow && player.lastSimultaneousSkillCast != null && player.lastSimultaneousSkillCast.itemConsumeId == itemId) {
            player.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM)
            return
        }

        if (!itemToRemove.isDestroyable || CursedWeaponManager.isCursed(itemId)) {
            player.sendPacket(if (itemToRemove.isHeroItem) SystemMessageId.HERO_WEAPONS_CANT_DESTROYED else SystemMessageId.CANNOT_DISCARD_THIS_ITEM)
            return
        }

        if (itemToRemove.isEquipped && (!itemToRemove.isStackable || itemToRemove.isStackable && _count >= itemToRemove.count)) {
            val unequipped = player.inventory!!.unEquipItemInSlotAndRecord(itemToRemove.locationSlot)
            val iu = InventoryUpdate()
            for (item in unequipped) {
                item.unChargeAllShots()
                iu.addModifiedItem(item)
            }

            player.sendPacket(iu)
            player.broadcastUserInfo()
        }

        // if it's a pet control item.
        if (itemToRemove.itemType === EtcItemType.PET_COLLAR) {
            // See if pet or mount is active ; can't destroy item linked to that pet.
            if (player.pet != null && player.pet!!.controlItemId == _objectId || player.isMounted && player.mountObjectId == _objectId) {
                player.sendPacket(SystemMessageId.PET_SUMMONED_MAY_NOT_DESTROYED)
                return
            }

            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(DELETE_PET).use { ps ->
                        ps.setInt(1, _objectId)
                        ps.execute()
                    }
                }
            } catch (e: Exception) {
                L2GameClientPacket.LOGGER.error("Couldn't delete pet item with objectid {}.", e, _objectId)
            }

        }

        player.destroyItem("Destroy", _objectId, _count, player, true)
    }

    companion object {
        private const val DELETE_PET = "DELETE FROM pets WHERE item_obj_id=?"
    }
}