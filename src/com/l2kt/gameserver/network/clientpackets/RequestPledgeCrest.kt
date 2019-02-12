package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.serverpackets.PledgeCrest

class RequestPledgeCrest : L2GameClientPacket() {
    private var _crestId: Int = 0

    override fun readImpl() {
        _crestId = readD()
    }

    override fun runImpl() {
        sendPacket(PledgeCrest(_crestId))
    }

    override fun triggersOnActionRequest(): Boolean {
        return false
    }
}