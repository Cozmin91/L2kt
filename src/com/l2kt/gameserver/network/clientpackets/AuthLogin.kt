package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.LoginServerThread
import com.l2kt.gameserver.network.SessionKey

class AuthLogin : L2GameClientPacket() {
    private var _loginName: String? = null
    private var _playKey1: Int = 0
    private var _playKey2: Int = 0
    private var _loginKey1: Int = 0
    private var _loginKey2: Int = 0

    override fun readImpl() {
        _loginName = readS().toLowerCase()
        _playKey2 = readD()
        _playKey1 = readD()
        _loginKey1 = readD()
        _loginKey2 = readD()
    }

    override fun runImpl() {
        if (client.accountName != null)
            return

        client.accountName = _loginName
        client.sessionId = SessionKey(_loginKey1, _loginKey2, _playKey1, _playKey2)

        LoginServerThread.addClient(_loginName!!, client)
    }
}