package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.Boat
import com.l2kt.gameserver.model.actor.stat.BoatStat

class VehicleDeparture(boat: Boat) : L2GameServerPacket() {
    private val _objectId: Int = boat.objectId
    private val _x: Int = boat.xdestination
    private val _y: Int = boat.ydestination
    private val _z: Int = boat.zdestination
    private val _moveSpeed: Int = boat.stat.moveSpeed.toInt()
    private val _rotationSpeed: Int = (boat.stat as BoatStat).rotationSpeed

    override fun writeImpl() {
        writeC(0x5A)
        writeD(_objectId)
        writeD(_moveSpeed)
        writeD(_rotationSpeed)
        writeD(_x)
        writeD(_y)
        writeD(_z)
    }
}