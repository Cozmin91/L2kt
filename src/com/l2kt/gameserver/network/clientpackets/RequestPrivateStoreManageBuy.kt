package com.l2kt.gameserver.network.clientpackets

class RequestPrivateStoreManageBuy : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val player = client.activeChar ?: return

        player.tryOpenPrivateBuyStore()
    }
}