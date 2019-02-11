package com.l2kt.gameserver.network.serverpackets

class Snoop(
    private val _convoId: Int,
    private val _name: String,
    private val _type: Int,
    private val _speaker: String,
    private val _msg: String
) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xd5)

        writeD(_convoId)
        writeS(_name)
        writeD(0x00) // ??
        writeD(_type)
        writeS(_speaker)
        writeS(_msg)
    }
}