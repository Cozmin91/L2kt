package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player

class MoveToLocationInVehicle(
    player: Player,
    private val _targetX: Int,
    private val _targetY: Int,
    private val _targetZ: Int,
    private val _originX: Int,
    private val _originY: Int,
    private val _originZ: Int
) : L2GameServerPacket() {
    private val _objectId: Int = player.objectId
    private val _boatId: Int = player.boat?.objectId ?: 0

    override fun writeImpl() {
        writeC(0x71)
        writeD(_objectId)
        writeD(_boatId)
        writeD(_targetX)
        writeD(_targetY)
        writeD(_targetZ)
        writeD(_originX)
        writeD(_originY)
        writeD(_originZ)
    }
}