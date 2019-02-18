package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.serverpackets.StopMoveInVehicle

class CannotMoveAnymoreInVehicle : L2GameClientPacket() {
    private var _boatId: Int = 0
    private var _x: Int = 0
    private var _y: Int = 0
    private var _z: Int = 0
    private var _heading: Int = 0

    override fun readImpl() {
        _boatId = readD()
        _x = readD()
        _y = readD()
        _z = readD()
        _heading = readD()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        if (player.isInBoat && player.boat?.objectId == _boatId) {
            player.boatPosition.set(_x, _y, _z, _heading)
            player.broadcastPacket(StopMoveInVehicle(player, _boatId))
        }
    }
}