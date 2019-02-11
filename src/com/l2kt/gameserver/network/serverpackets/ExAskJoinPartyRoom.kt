package com.l2kt.gameserver.network.serverpackets

/**
 * Format: ch S
 * @author KenM
 */
class ExAskJoinPartyRoom(private val _charName: String) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x34)
        writeS(_charName)
    }
}