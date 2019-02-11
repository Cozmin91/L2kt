package com.l2kt.gameserver.network.serverpackets

class AutoAttackStop(private val _targetObjId: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x2c)
        writeD(_targetObjId)
    }
}