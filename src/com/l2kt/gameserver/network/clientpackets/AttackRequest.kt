package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ActionFailed

class AttackRequest : L2GameClientPacket() {
    // cddddc
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
        val activeChar = client.activeChar ?: return

        if (activeChar.isInObserverMode) {
            activeChar.sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE)
            activeChar.sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        // avoid using expensive operations if not needed
        val target: WorldObject?
        target = if (activeChar.targetId == _objectId)
            activeChar.target
        else
            World.getInstance().getObject(_objectId)

        if (target == null) {
            activeChar.sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        if (activeChar.target !== target)
            target.onAction(activeChar)
        else {
            if (target.objectId != activeChar.objectId && !activeChar.isInStoreMode && activeChar.activeRequester == null)
                target.onForcedAttack(activeChar)
            else
                sendPacket(ActionFailed.STATIC_PACKET)
        }
    }
}