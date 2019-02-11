package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.EnchantResult
import com.l2kt.gameserver.network.serverpackets.StopMove
import java.nio.BufferUnderflowException

class MoveBackwardToLocation : L2GameClientPacket() {
    private var _targetX: Int = 0
    private var _targetY: Int = 0
    private var _targetZ: Int = 0
    private var _originX: Int = 0
    private var _originY: Int = 0
    private var _originZ: Int = 0

    private var _moveMovement: Int = 0

    override fun readImpl() {
        _targetX = readD()
        _targetY = readD()
        _targetZ = readD()
        _originX = readD()
        _originY = readD()
        _originZ = readD()

        try {
            _moveMovement = readD() // is 0 if cursor keys are used 1 if mouse is used
        } catch (e: BufferUnderflowException) {
            if (Config.L2WALKER_PROTECTION) {
                val player = client.activeChar
                player?.logout(false)
            }
        }

    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        if (activeChar.isOutOfControl) {
            activeChar.sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        if (activeChar.activeEnchantItem != null) {
            activeChar.activeEnchantItem = null
            activeChar.sendPacket(EnchantResult.CANCELLED)
            activeChar.sendPacket(SystemMessageId.ENCHANT_SCROLL_CANCELLED)
        }

        if (_targetX == _originX && _targetY == _originY && _targetZ == _originZ) {
            activeChar.sendPacket(StopMove(activeChar))
            return
        }

        // Correcting targetZ from floor level to head level
        _targetZ += activeChar.collisionHeight.toInt()

        if (activeChar.teleMode > 0) {
            if (activeChar.teleMode == 1)
                activeChar.teleMode = 0

            activeChar.sendPacket(ActionFailed.STATIC_PACKET)
            activeChar.teleToLocation(_targetX, _targetY, _targetZ, 0)
            return
        }

        val dx = (_targetX - _originX).toDouble()
        val dy = (_targetY - _originY).toDouble()

        if (dx * dx + dy * dy > 98010000)
        // 9900*9900
        {
            activeChar.sendPacket(ActionFailed.STATIC_PACKET)
            return
        }
        activeChar.ai.setIntention(CtrlIntention.MOVE_TO, Location(_targetX, _targetY, _targetZ))
    }
}