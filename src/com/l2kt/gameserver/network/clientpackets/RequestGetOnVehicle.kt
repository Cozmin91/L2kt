package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.manager.BoatManager
import com.l2kt.gameserver.model.actor.Boat
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.GetOnVehicle

class RequestGetOnVehicle : L2GameClientPacket() {
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

        val boat: Boat?
        if (activeChar.isInBoat) {
            boat = activeChar.boat
            if (boat!!.objectId != _boatId) {
                sendPacket(ActionFailed.STATIC_PACKET)
                return
            }
        } else {
            boat = BoatManager.getBoat(_boatId)
            if (boat == null || boat.isMoving || !activeChar.isInsideRadius(boat, 1000, true, false)) {
                sendPacket(ActionFailed.STATIC_PACKET)
                return
            }
        }

        activeChar.boatPosition.set(_x, _y, _z, activeChar.heading)
        activeChar.boat = boat
        activeChar.broadcastPacket(GetOnVehicle(activeChar.objectId, boat.objectId, _x, _y, _z))

        activeChar.setXYZ(boat.x, boat.y, boat.z)
        activeChar.revalidateZone(true)
    }
}