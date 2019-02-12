package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.network.serverpackets.GetOnVehicle
import com.l2kt.gameserver.network.serverpackets.ValidateLocation

class ValidatePosition : L2GameClientPacket() {
    private var _x: Int = 0
    private var _y: Int = 0
    private var _z: Int = 0
    private var _heading: Int = 0
    private var _data: Int = 0

    override fun readImpl() {
        _x = readD()
        _y = readD()
        _z = readD()
        _heading = readD()
        _data = readD()
    }

    override fun runImpl() {
        val player = client.activeChar
        if (player == null || player.isTeleporting || player.isInObserverMode)
            return

        val realX = player.x
        val realY = player.y
        var realZ = player.z

        if (_x == 0 && _y == 0) {
            if (realX != 0)
            // in this case this seems like a client error
                return
        }

        val dx: Int
        val dy: Int
        val dz: Int
        val diffSq: Double

        if (player.isInBoat) {
            dx = _x - player.boatPosition.x
            dy = _y - player.boatPosition.y
            dz = _z - player.boatPosition.z
            diffSq = (dx * dx + dy * dy).toDouble()

            if (diffSq > 250000)
                sendPacket(GetOnVehicle(player.objectId, _data, player.boatPosition))

            return
        }

        if (player.isFalling(_z))
            return  // disable validations during fall to avoid "jumping"

        dx = _x - realX
        dy = _y - realY
        dz = _z - realZ
        diffSq = (dx * dx + dy * dy).toDouble()

        if (player.isFlying || player.isInsideZone(ZoneId.WATER)) {
            player.setXYZ(realX, realY, _z)
            if (diffSq > 90000)
            // validate packet, may also cause z bounce if close to land
                player.sendPacket(ValidateLocation(player))
        } else if (diffSq < 360000)
        // if too large, messes observation
        {
            if (diffSq > 250000 || Math.abs(dz) > 200) {
                if (Math.abs(dz) in 201..1499 && Math.abs(_z - player.clientZ) < 800) {
                    player.setXYZ(realX, realY, _z)
                    realZ = _z
                } else
                    player.sendPacket(ValidateLocation(player))
            }
        }

        player.clientX = _x
        player.clientY = _y
        player.clientZ = _z
        player.clientHeading = _heading // No real need to validate heading.
    }
}