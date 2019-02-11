package com.l2kt.gameserver.network.serverpackets

class CharDeleteFail(private val _error: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x24)
        writeD(_error)
    }

    companion object {
        val REASON_DELETION_FAILED = CharDeleteFail(0x01)
        val REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER = CharDeleteFail(0x02)
        val REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED = CharDeleteFail(0x03)
    }
}