package com.l2kt.gameserver.network.serverpackets

class RestartResponse(private val _result: Boolean) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x5f)
        writeD(if (_result) 1 else 0)
    }

    companion object {
        private val STATIC_PACKET_TRUE = RestartResponse(true)
        private val STATIC_PACKET_FALSE = RestartResponse(false)

        fun valueOf(result: Boolean): RestartResponse {
            return if (result) STATIC_PACKET_TRUE else STATIC_PACKET_FALSE
        }
    }
}