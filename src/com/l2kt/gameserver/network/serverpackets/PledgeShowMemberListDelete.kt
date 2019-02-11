package com.l2kt.gameserver.network.serverpackets

class PledgeShowMemberListDelete(private val _player: String) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x56)
        writeS(_player)
    }
}