package com.l2kt.loginserver.network.serverpackets

import com.l2kt.loginserver.network.SessionKey

/**
 * Format: dddddddd f: the session key d: ? d: ? d: ? d: ? d: ? d: ? b: 16 bytes - unknown
 */
class LoginOk(sessionKey: SessionKey) : L2LoginServerPacket() {
    private val _loginOk1: Int
    private val _loginOk2: Int

    init {
        _loginOk1 = sessionKey.loginOkID1
        _loginOk2 = sessionKey.loginOkID2
    }

    override fun write() {
        writeC(0x03)
        writeD(_loginOk1)
        writeD(_loginOk2)
        writeD(0x00)
        writeD(0x00)
        writeD(0x000003ea)
        writeD(0x00)
        writeD(0x00)
        writeD(0x00)
        writeB(ByteArray(16))
    }
}