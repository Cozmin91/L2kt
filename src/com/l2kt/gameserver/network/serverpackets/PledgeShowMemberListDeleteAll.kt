package com.l2kt.gameserver.network.serverpackets

class PledgeShowMemberListDeleteAll private constructor() : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x82)
    }

    companion object {
        val STATIC_PACKET = PledgeShowMemberListDeleteAll()
    }
}