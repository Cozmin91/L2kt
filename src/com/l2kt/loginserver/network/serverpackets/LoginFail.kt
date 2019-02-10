package com.l2kt.loginserver.network.serverpackets

class LoginFail private constructor(private val _reason: Int) : L2LoginServerPacket() {

    override fun write() {
        writeC(0x01)
        writeD(_reason)
    }

    companion object {
        val REASON_SYSTEM_ERROR = LoginFail(0x01)
        val REASON_PASS_WRONG = LoginFail(0x02)
        val REASON_USER_OR_PASS_WRONG = LoginFail(0x03)
        val REASON_ACCESS_FAILED = LoginFail(0x04)
        val REASON_ACCOUNT_IN_USE = LoginFail(0x07)
        val REASON_SERVER_OVERLOADED = LoginFail(0x0f)
        val REASON_SERVER_MAINTENANCE = LoginFail(0x10)
        val REASON_TEMP_PASS_EXPIRED = LoginFail(0x11)
        val REASON_DUAL_BOX = LoginFail(0x23)
    }
}