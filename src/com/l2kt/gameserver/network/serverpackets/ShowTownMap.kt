package com.l2kt.gameserver.network.serverpackets

class ShowTownMap(private val _texture: String, private val _x: Int, private val _y: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xde)
        writeS(_texture)
        writeD(_x)
        writeD(_y)
    }
}