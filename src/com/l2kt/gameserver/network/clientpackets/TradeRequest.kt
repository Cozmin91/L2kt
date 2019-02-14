package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.commons.math.MathUtil
import com.l2kt.gameserver.model.BlockList
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId

import com.l2kt.gameserver.network.serverpackets.SendTradeRequest
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class TradeRequest : L2GameClientPacket() {
    private var _objectId: Int = 0

    override fun readImpl() {
        _objectId = readD()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        if (!player.accessLevel.allowTransaction) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
            return
        }

        val target = World.getInstance().getPlayer(_objectId)
        if (target == null || !player.getKnownType(Player::class.java).contains(target) || target == player) {
            player.sendPacket(SystemMessageId.TARGET_IS_INCORRECT)
            return
        }

        if (target.isInOlympiadMode || player.isInOlympiadMode) {
            player.sendMessage("You or your target cannot trade during Olympiad.")
            return
        }

        // Alt game - Karma punishment
        if (!Config.KARMA_PLAYER_CAN_TRADE && (player.karma > 0 || target.karma > 0)) {
            player.sendMessage("You cannot trade in a chaotic state.")
            return
        }

        if (player.isInStoreMode || target.isInStoreMode) {
            player.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE)
            return
        }

        if (player.isProcessingTransaction) {
            player.sendPacket(SystemMessageId.ALREADY_TRADING)
            return
        }

        if (target.isProcessingRequest || target.isProcessingTransaction) {
            val sm = SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addCharName(target)
            player.sendPacket(sm)
            return
        }

        if (target.tradeRefusal) {
            player.sendMessage("Your target is in trade refusal mode.")
            return
        }

        if (BlockList.isBlocked(target, player)) {
            val sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST).addCharName(target)
            player.sendPacket(sm)
            return
        }

        if (MathUtil.calculateDistance(player, target, true) > Npc.INTERACTION_DISTANCE) {
            player.sendPacket(SystemMessageId.TARGET_TOO_FAR)
            return
        }

        player.onTransactionRequest(target)
        target.sendPacket(SendTradeRequest(player.objectId))
        player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REQUEST_S1_FOR_TRADE).addCharName(target))
    }
}