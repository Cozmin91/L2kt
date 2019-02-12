package com.l2kt.gameserver.network.clientpackets

class RequestPrivateStoreManageSell : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val player = client.activeChar ?: return

        player.tryOpenPrivateSellStore(false)
    }
}