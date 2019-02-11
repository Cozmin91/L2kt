package com.l2kt.gameserver.network.serverpackets

class ActionFailed private constructor() : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x25)
    }

    companion object {
        val STATIC_PACKET = ActionFailed()
    }
}