package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.RecipeShopSellList

class RequestRecipeShopManagePrev : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val player = client.activeChar ?: return

        // Player shouldn't be able to set stores if he/she is alike dead (dead or fake death)
        if (player.isAlikeDead) {
            sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        if (player.target !is Player)
            return

        player.sendPacket(RecipeShopSellList(player, player.target as Player))
    }
}