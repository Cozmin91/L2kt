package com.l2kt.gameserver.network.serverpackets

class TargetSelected(
    private val _objectId: Int,
    private val _targetObjId: Int,
    private val _x: Int,
    private val _y: Int,
    private val _z: Int
) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x29)
        writeD(_objectId)
        writeD(_targetObjId)
        writeD(_x)
        writeD(_y)
        writeD(_z)
    }
}