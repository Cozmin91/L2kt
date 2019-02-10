package com.l2kt.loginserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.loginserver.LoginController
import com.l2kt.loginserver.network.serverpackets.LoginFail
import com.l2kt.loginserver.network.serverpackets.PlayFail
import com.l2kt.loginserver.network.serverpackets.PlayOk

class RequestServerLogin : L2LoginClientPacket() {
    var sessionKey1: Int = 0
        private set
    var sessionKey2: Int = 0
        private set
    var serverID: Int = 0
        private set

    public override fun readImpl(): Boolean {
        if (super._buf.remaining() >= 9) {
            sessionKey1 = readD()
            sessionKey2 = readD()
            serverID = readC()
            return true
        }
        return false
    }

    override fun run() {
        val sk = client.sessionKey

        // if we didnt showed the license we cant check these values
        if (!Config.SHOW_LICENCE || sk!!.checkLoginPair(sessionKey1, sessionKey2)) {
            if (LoginController.isLoginPossible(client, serverID)) {
                client.setJoinedGS(true)
                client.sendPacket(PlayOk(sk!!))
            } else
                client.close(PlayFail.REASON_TOO_MANY_PLAYERS)
        } else
            client.close(LoginFail.REASON_ACCESS_FAILED)
    }
}
