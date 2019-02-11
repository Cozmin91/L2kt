package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.commons.math.MathUtil
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Pet
import com.l2kt.gameserver.model.item.type.EtcItemType
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.EnchantResult

class RequestGiveItemToPet : L2GameClientPacket() {
    private var _objectId: Int = 0
    private var _amount: Int = 0

    override fun readImpl() {
        _objectId = readD()
        _amount = readD()
    }

    override fun runImpl() {
        if (_amount <= 0)
            return

        val player = client.activeChar
        if (player == null || !player.hasPet())
            return

        // Alt game - Karma punishment
        if (!Config.KARMA_PLAYER_CAN_TRADE && player.karma > 0) {
            player.sendMessage("You cannot trade in a chaotic state.")
            return
        }

        if (player.isInStoreMode) {
            player.sendPacket(SystemMessageId.CANNOT_PICKUP_OR_USE_ITEM_WHILE_TRADING)
            return
        }

        if (player.isProcessingTransaction) {
            player.sendPacket(SystemMessageId.ALREADY_TRADING)
            return
        }

        val item = player.inventory!!.getItemByObjectId(_objectId)
        if (item == null || item.isAugmented)
            return

        if (item.isHeroItem || !item.isDropable || !item.isDestroyable || !item.isTradable || item.item.itemType === EtcItemType.ARROW || item.item.itemType === EtcItemType.SHOT) {
            player.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS)
            return
        }

        val pet = player.pet as Pet
        if (pet.isDead) {
            player.sendPacket(SystemMessageId.CANNOT_GIVE_ITEMS_TO_DEAD_PET)
            return
        }

        if (MathUtil.calculateDistance(player, pet, true) > Npc.INTERACTION_DISTANCE) {
            player.sendPacket(SystemMessageId.TARGET_TOO_FAR)
            return
        }

        if (!pet.inventory!!.validateCapacity(item)) {
            player.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS)
            return
        }

        if (!pet.inventory!!.validateWeight(item, _amount)) {
            player.sendPacket(SystemMessageId.UNABLE_TO_PLACE_ITEM_YOUR_PET_IS_TOO_ENCUMBERED)
            return
        }

        if (player.activeEnchantItem != null) {
            player.activeEnchantItem = null
            player.sendPacket(EnchantResult.CANCELLED)
            player.sendPacket(SystemMessageId.ENCHANT_SCROLL_CANCELLED)
        }

        player.transferItem("Transfer", _objectId, _amount, pet.inventory, pet)
    }
}