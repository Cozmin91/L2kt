package com.l2kt.gameserver.network.serverpackets

class PetDelete(private val _summonType: Int, private val _objId: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xb6)
        writeD(_summonType)
        writeD(_objId)
    }
}