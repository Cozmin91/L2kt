package com.l2kt.gameserver.network.serverpackets

class AutoAttackStart(private val _targetObjId: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x2b)
        writeD(_targetObjId)
    }
}