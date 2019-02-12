package com.l2kt.gameserver.network.clientpackets

class RequestRecipeShopManageList : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val player = client.activeChar ?: return

        player.tryOpenWorkshop(true)
    }
}