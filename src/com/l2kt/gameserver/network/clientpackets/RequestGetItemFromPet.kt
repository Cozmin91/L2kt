package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.actor.instance.Pet
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.EnchantResult

class RequestGetItemFromPet : L2GameClientPacket() {
    private var _objectId: Int = 0
    private var _amount: Int = 0
    private var _unknown: Int = 0

    override fun readImpl() {
        _objectId = readD()
        _amount = readD()
        _unknown = readD()// = 0 for most trades
    }

    override fun runImpl() {
        if (_amount <= 0)
            return

        val player = client.activeChar
        if (player == null || !player.hasPet())
            return

        if (player.isProcessingTransaction) {
            player.sendPacket(SystemMessageId.ALREADY_TRADING)
            return
        }

        if (player.activeEnchantItem != null) {
            player.activeEnchantItem = null
            player.sendPacket(EnchantResult.CANCELLED)
            player.sendPacket(SystemMessageId.ENCHANT_SCROLL_CANCELLED)
        }

        val pet = player.pet as Pet

        pet.transferItem("Transfer", _objectId, _amount, player.inventory, player, pet)
    }
}