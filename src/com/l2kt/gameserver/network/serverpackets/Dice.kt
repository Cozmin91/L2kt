package com.l2kt.gameserver.network.serverpackets

class Dice(
    private val _charObjId: Int,
    private val _itemId: Int,
    private val _number: Int,
    private val _x: Int,
    private val _y: Int,
    private val _z: Int
) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xD4)
        writeD(_charObjId)
        writeD(_itemId)
        writeD(_number)
        writeD(_x)
        writeD(_y)
        writeD(_z)
    }
}