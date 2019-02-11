package com.l2kt.gameserver.network.serverpackets

class MyTargetSelected(private val _objectId: Int, private val _color: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xa6)
        writeD(_objectId)
        writeH(_color)
    }
}