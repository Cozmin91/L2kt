package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.Creature

/**
 * @author Kerberos
 */
class VehicleStarted(boat: Creature, private val _state: Int) : L2GameServerPacket() {
    private val _objectId: Int = boat.objectId

    override fun writeImpl() {
        writeC(0xBA)
        writeD(_objectId)
        writeD(_state)
    }
}