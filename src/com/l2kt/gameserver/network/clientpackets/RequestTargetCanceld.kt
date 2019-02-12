package com.l2kt.gameserver.network.clientpackets

class RequestTargetCanceld : L2GameClientPacket() {
    private var _unselect: Int = 0

    override fun readImpl() {
        _unselect = readH()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        if (_unselect == 0) {
            if (activeChar.isCastingNow && activeChar.canAbortCast())
                activeChar.abortCast()
            else
                activeChar.target = null
        } else
            activeChar.target = null
    }
}