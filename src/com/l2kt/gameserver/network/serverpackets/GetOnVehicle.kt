package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.location.SpawnLocation

class GetOnVehicle : L2GameServerPacket {
    private val _objectId: Int
    private val _boatId: Int
    private val _x: Int
    private val _y: Int
    private val _z: Int

    constructor(objectId: Int, boatId: Int, x: Int, y: Int, z: Int) {
        _objectId = objectId
        _boatId = boatId
        _x = x
        _y = y
        _z = z
    }

    constructor(objectId: Int, boatId: Int, loc: SpawnLocation) {
        _objectId = objectId
        _boatId = boatId
        _x = loc.x
        _y = loc.y
        _z = loc.z
    }

    override fun writeImpl() {
        writeC(0x5C)
        writeD(_objectId)
        writeD(_boatId)
        writeD(_x)
        writeD(_y)
        writeD(_z)
    }
}