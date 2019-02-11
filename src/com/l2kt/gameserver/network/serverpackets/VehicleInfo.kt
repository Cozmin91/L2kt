package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.Boat

class VehicleInfo(boat: Boat) : L2GameServerPacket() {
    private val _objectId: Int
    private val _x: Int
    private val _y: Int
    private val _z: Int
    private val _heading: Int

    init {
        _objectId = boat.objectId
        _x = boat.x
        _y = boat.y
        _z = boat.z
        _heading = boat.heading
    }

    override fun writeImpl() {
        writeC(0x59)
        writeD(_objectId)
        writeD(_x)
        writeD(_y)
        writeD(_z)
        writeD(_heading)
    }
}