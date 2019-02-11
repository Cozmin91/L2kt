package com.l2kt.gameserver.network.serverpackets

class AllyCrest(private val _crestId: Int, private val _data: ByteArray) : L2GameServerPacket() {
    private val _crestSize: Int = _data.size

    override fun writeImpl() {
        writeC(0xae)
        writeD(_crestId)
        writeD(_crestSize)
        writeB(_data)
    }
}