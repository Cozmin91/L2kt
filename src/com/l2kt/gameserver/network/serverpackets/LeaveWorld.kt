package com.l2kt.gameserver.network.serverpackets

class LeaveWorld private constructor() : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x7e)
    }

    companion object {
        val STATIC_PACKET = LeaveWorld()
    }
}