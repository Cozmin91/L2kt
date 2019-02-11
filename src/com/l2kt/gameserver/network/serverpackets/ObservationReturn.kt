package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.location.Location

class ObservationReturn(private val _location: Location) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xe0)
        writeD(_location.x)
        writeD(_location.y)
        writeD(_location.z)
    }
}