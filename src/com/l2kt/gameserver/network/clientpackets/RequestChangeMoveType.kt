package com.l2kt.gameserver.network.clientpackets

class RequestChangeMoveType : L2GameClientPacket() {
    private var _typeRun: Boolean = false

    override fun readImpl() {
        _typeRun = readD() == 1
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        if (player.isMounted)
            return

        if (_typeRun)
            player.setRunning()
        else
            player.setWalking()
    }
}