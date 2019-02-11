package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.location.SpawnLocation

class ValidateLocationInVehicle(player: Player) : L2GameServerPacket() {
    private val _objectId: Int = player.objectId
    private val _boatId: Int = player.boat.objectId
    private val _loc: SpawnLocation = player.boatPosition

    override fun writeImpl() {
        writeC(0x73)
        writeD(_objectId)
        writeD(_boatId)
        writeD(_loc.x)
        writeD(_loc.y)
        writeD(_loc.z)
        writeD(_loc.heading)
    }
}