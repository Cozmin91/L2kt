package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.instancemanager.SevenSigns

class ShowMiniMap(private val _mapId: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x9d)
        writeD(_mapId)
        writeD(SevenSigns.currentPeriod.ordinal)
    }

    companion object {
        val REGULAR_MAP = ShowMiniMap(1665)
    }
}