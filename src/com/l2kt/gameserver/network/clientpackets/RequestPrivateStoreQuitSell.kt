package com.l2kt.gameserver.network.clientpackets

class RequestPrivateStoreQuitSell : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val player = client.activeChar ?: return

        player.forceStandUp()
    }
}