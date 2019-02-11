package com.l2kt.gameserver.network.serverpackets

class KeyPacket(private val _key: ByteArray) : L2GameServerPacket() {

    public override fun writeImpl() {
        writeC(0x00)
        writeC(0x01)
        writeB(_key)
        writeD(0x01)
        writeD(0x01)
    }
}