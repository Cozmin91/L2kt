package com.l2kt.loginserver.network.clientpackets

import com.l2kt.loginserver.network.serverpackets.LoginFail
import com.l2kt.loginserver.network.serverpackets.ServerList

class RequestServerList : L2LoginClientPacket() {
    var sessionKey1: Int = 0
        private set
    var sessionKey2: Int = 0
        private set
    val data3: Int = 0

    public override fun readImpl(): Boolean {
        if (super._buf.remaining() >= 8) {
            sessionKey1 = readD() // loginOk 1
            sessionKey2 = readD() // loginOk 2
            return true
        }
        return false
    }

    override fun run() {
        if (client.sessionKey!!.checkLoginPair(sessionKey1, sessionKey2))
            client.sendPacket(ServerList(client))
        else
            client.close(LoginFail.REASON_ACCESS_FAILED)
    }
}