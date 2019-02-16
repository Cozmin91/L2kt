package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.instancemanager.SevenSigns
import com.l2kt.gameserver.instancemanager.SevenSigns.CabalType

class SSQInfo private constructor(private val _state: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xf8)
        writeH(_state)
    }

    companion object {
        val REGULAR_SKY_PACKET = SSQInfo(256)
        val DUSK_SKY_PACKET = SSQInfo(257)
        val DAWN_SKY_PACKET = SSQInfo(258)
        val RED_SKY_PACKET = SSQInfo(259)

        fun sendSky(): SSQInfo {
            if (SevenSigns.isSealValidationPeriod) {
                val winningCabal = SevenSigns.cabalHighestScore
                if (winningCabal == CabalType.DAWN)
                    return DAWN_SKY_PACKET

                if (winningCabal == CabalType.DUSK)
                    return DUSK_SKY_PACKET
            }
            return REGULAR_SKY_PACKET
        }
    }
}