package com.l2kt.gameserver.network.serverpackets

class ExAutoSoulShot(private val _itemId: Int, private val _type: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xFE)
        writeH(0x12)
        writeD(_itemId)
        writeD(_type)
    }
}