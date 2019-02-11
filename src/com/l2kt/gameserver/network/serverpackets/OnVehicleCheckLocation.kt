package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.Creature

/**
 * @author Maktakien
 */
class OnVehicleCheckLocation(private val _boat: Creature) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x5b)
        writeD(_boat.objectId)
        writeD(_boat.x)
        writeD(_boat.y)
        writeD(_boat.z)
        writeD(_boat.heading)
    }
}