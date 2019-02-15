package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.manager.BoatManager
import com.l2kt.gameserver.model.actor.Boat
import com.l2kt.gameserver.model.item.type.WeaponType
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.MoveToLocationInVehicle
import com.l2kt.gameserver.network.serverpackets.StopMoveInVehicle

class RequestMoveToLocationInVehicle : L2GameClientPacket() {
    private var _boatId: Int = 0
    private var _targetX: Int = 0
    private var _targetY: Int = 0
    private var _targetZ: Int = 0
    private var _originX: Int = 0
    private var _originY: Int = 0
    private var _originZ: Int = 0

    override fun readImpl() {
        _boatId = readD()
        _targetX = readD()
        _targetY = readD()
        _targetZ = readD()
        _originX = readD()
        _originY = readD()
        _originZ = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        if (_targetX == _originX && _targetY == _originY && _targetZ == _originZ) {
            activeChar.sendPacket(StopMoveInVehicle(activeChar, _boatId))
            return
        }

        if (activeChar.isAttackingNow && activeChar.attackType == WeaponType.BOW) {
            activeChar.sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        if (activeChar.isSitting || activeChar.isMovementDisabled) {
            activeChar.sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        if (activeChar.pet != null) {
            activeChar.sendPacket(SystemMessageId.RELEASE_PET_ON_BOAT)
            activeChar.sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        val boat: Boat?
        if (activeChar.isInBoat) {
            boat = activeChar.boat
            if (boat!!.objectId != _boatId) {
                activeChar.sendPacket(ActionFailed.STATIC_PACKET)
                return
            }
        } else {
            boat = BoatManager.getBoat(_boatId)
            if (boat == null || !boat.isInsideRadius(activeChar, 300, true, false)) {
                activeChar.sendPacket(ActionFailed.STATIC_PACKET)
                return
            }
            activeChar.boat = boat
        }

        activeChar.boatPosition.set(_targetX, _targetY, _targetZ)
        activeChar.broadcastPacket(
            MoveToLocationInVehicle(
                activeChar,
                _targetX,
                _targetY,
                _targetZ,
                _originX,
                _originY,
                _originZ
            )
        )
    }
}