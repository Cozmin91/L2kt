package com.l2kt.loginserver.network.clientpackets

import com.l2kt.loginserver.network.LoginClient
import com.l2kt.loginserver.network.serverpackets.GGAuth
import com.l2kt.loginserver.network.serverpackets.LoginFail

class AuthGameGuard : L2LoginClientPacket() {
    var sessionId: Int = 0
        private set
    var data1: Int = 0
        private set
    var data2: Int = 0
        private set
    var data3: Int = 0
        private set
    var data4: Int = 0
        private set

    override fun readImpl(): Boolean {
        if (super._buf.remaining() >= 20) {
            sessionId = readD()
            data1 = readD()
            data2 = readD()
            data3 = readD()
            data4 = readD()
            return true
        }
        return false
    }

    override fun run() {
        if (sessionId == client.sessionId) {
            client.state = LoginClient.LoginClientState.AUTHED_GG
            client.sendPacket(GGAuth(client.sessionId))
        } else
            client.close(LoginFail.REASON_ACCESS_FAILED)
    }
}