package com.l2kt.loginserver.network.serverpackets

class PlayFail private constructor(private val _reason: Int) : L2LoginServerPacket() {

    override fun write() {
        writeC(0x06)
        writeC(_reason)
    }

    companion object {
        val REASON_SYSTEM_ERROR = PlayFail(0x01)
        val REASON_USER_OR_PASS_WRONG = PlayFail(0x02)
        val REASON3 = PlayFail(0x03)
        val REASON4 = PlayFail(0x04)
        val REASON_TOO_MANY_PLAYERS = PlayFail(0x0f)
    }
}