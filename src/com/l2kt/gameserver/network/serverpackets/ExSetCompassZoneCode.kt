package com.l2kt.gameserver.network.serverpackets

/**
 * Format: ch d
 * @author KenM
 */
class ExSetCompassZoneCode(private val _zoneType: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xFE)
        writeH(0x32)
        writeD(_zoneType)
    }

    companion object {
        const val SIEGEWARZONE1 = 0x0A
        const val SIEGEWARZONE2 = 0x0B
        const val PEACEZONE = 0x0C
        const val SEVENSIGNSZONE = 0x0D
        const val PVPZONE = 0x0E
        const val GENERALZONE = 0x0F
    }
}