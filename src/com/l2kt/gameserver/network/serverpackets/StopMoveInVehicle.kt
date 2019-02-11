package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.location.SpawnLocation

class StopMoveInVehicle(player: Player, private val _boatId: Int) : L2GameServerPacket() {
    private val _objectId: Int = player.objectId
    private val _loc: SpawnLocation = player.boatPosition

    override fun writeImpl() {
        writeC(0x72)
        writeD(_objectId)
        writeD(_boatId)
        writeD(_loc.x)
        writeD(_loc.y)
        writeD(_loc.z)
        writeD(_loc.heading)
    }
}