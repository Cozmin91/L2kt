package com.l2kt.gameserver.network.clientpackets

class RequestChangeWaitType : L2GameClientPacket() {
    private var _typeStand: Boolean = false

    override fun readImpl() {
        _typeStand = readD() == 1
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        player.tryToSitOrStand(player.target, _typeStand)
    }
}