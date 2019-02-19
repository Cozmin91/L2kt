package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.GetOffVehicle
import com.l2kt.gameserver.network.serverpackets.StopMoveInVehicle

class RequestGetOffVehicle : L2GameClientPacket() {
    private var _boatId: Int = 0
    private var _x: Int = 0
    private var _y: Int = 0
    private var _z: Int = 0

    override fun readImpl() {
        _boatId = readD()
        _x = readD()
        _y = readD()
        _z = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        if (!activeChar.isInBoat || activeChar.boat!!.objectId != _boatId || activeChar.boat!!.isMoving || !activeChar.isInsideRadius(
                _x,
                _y,
                _z,
                1000,
                true,
                false
            )
        ) {
            sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        activeChar.broadcastPacket(StopMoveInVehicle(activeChar, _boatId))
        activeChar.boat = null
        sendPacket(ActionFailed.STATIC_PACKET)
        activeChar.broadcastPacket(GetOffVehicle(activeChar.objectId, _boatId, _x, _y, _z))
        activeChar.setXYZ(_x, _y, _z + 50)
        activeChar.revalidateZone(true)
    }
}