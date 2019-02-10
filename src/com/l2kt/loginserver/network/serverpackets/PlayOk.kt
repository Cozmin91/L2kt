package com.l2kt.loginserver.network.serverpackets

import com.l2kt.loginserver.network.SessionKey

class PlayOk(sessionKey: SessionKey) : L2LoginServerPacket() {
    private val _playOk1: Int
    private val _playOk2: Int

    init {
        _playOk1 = sessionKey.playOkID1
        _playOk2 = sessionKey.playOkID2
    }

    override fun write() {
        writeC(0x07)
        writeD(_playOk1)
        writeD(_playOk2)
    }
}