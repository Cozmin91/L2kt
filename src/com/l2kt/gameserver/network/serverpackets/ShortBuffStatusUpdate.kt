package com.l2kt.gameserver.network.serverpackets

class ShortBuffStatusUpdate(private val _skillId: Int, private val _skillLvl: Int, private val _duration: Int) :
    L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xF4)
        writeD(_skillId)
        writeD(_skillLvl)
        writeD(_duration)
    }
}