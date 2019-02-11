package com.l2kt.gameserver.network.serverpackets

class ExPledgeCrestLarge(private val _crestId: Int, private val _data: ByteArray) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x28)

        writeD(0x00) // ???
        writeD(_crestId)

        if (_data.isNotEmpty()) {
            writeD(_data.size)
            writeB(_data)
        } else
            writeD(0x00)
    }
}