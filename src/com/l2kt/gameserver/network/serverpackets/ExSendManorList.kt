package com.l2kt.gameserver.network.serverpackets

/**
 * Format : (h) d [dS]
 * @author l3x
 */
class ExSendManorList private constructor() : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xFE)
        writeH(0x1B)
        writeD(manorList.size)
        for (i in manorList.indices) {
            writeD(i + 1)
            writeS(manorList[i])
        }
    }

    companion object {
        val STATIC_PACKET = ExSendManorList()

        private val manorList =
            arrayOf("gludio", "dion", "giran", "oren", "aden", "innadril", "goddard", "rune", "schuttgart")
    }
}