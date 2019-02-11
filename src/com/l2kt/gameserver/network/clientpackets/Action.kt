package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.entity.Duel
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ActionFailed

class Action : L2GameClientPacket() {
    private var _objectId: Int = 0
    private var _originX: Int = 0
    private var _originY: Int = 0
    private var _originZ: Int = 0
    private var _isShiftAction: Boolean = false

    override fun readImpl() {
        _objectId = readD()
        _originX = readD()
        _originY = readD()
        _originZ = readD()
        _isShiftAction = readC() != 0
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        if (player.isInObserverMode) {
            player.sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE)
            player.sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        if (player.activeRequester != null) {
            player.sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        val target = if (player.targetId == _objectId) player.target else World.getInstance().getObject(_objectId)
        if (target == null) {
            player.sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        val targetPlayer = target.actingPlayer
        if (targetPlayer != null && targetPlayer.duelState == Duel.DuelState.DEAD) {
            player.sendPacket(SystemMessageId.OTHER_PARTY_IS_FROZEN)
            player.sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        if (_isShiftAction)
            target.onActionShift(player)
        else
            target.onAction(player)
    }

    override fun triggersOnActionRequest(): Boolean {
        return false
    }
}