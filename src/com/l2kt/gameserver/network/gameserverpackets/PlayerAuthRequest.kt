package com.l2kt.gameserver.network.gameserverpackets

import com.l2kt.gameserver.network.SessionKey

class PlayerAuthRequest(account: String, key: SessionKey) : GameServerBasePacket() {

    override val content: ByteArray
        get() = bytes

    init {
        writeC(0x05)
        writeS(account)
        writeD(key.playOkID1)
        writeD(key.playOkID2)
        writeD(key.loginOkID1)
        writeD(key.loginOkID2)
    }
}