package com.l2kt.gameserver.network.serverpackets

/**
 * Format: ch Sd
 * @author KenM
 */
class ExDuelAskStart(private val _requestorName: String, private val _partyDuel: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x4b)

        writeS(_requestorName)
        writeD(_partyDuel)
    }
}