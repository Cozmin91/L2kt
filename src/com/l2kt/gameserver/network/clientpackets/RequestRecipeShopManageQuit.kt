package com.l2kt.gameserver.network.clientpackets

class RequestRecipeShopManageQuit : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val player = client.activeChar ?: return

        player.forceStandUp()
    }
}