package com.l2kt.gameserver.network.serverpackets

class MagicSkillCanceled(private val _objectId: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x49)
        writeD(_objectId)
    }
}