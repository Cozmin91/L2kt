package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.RecipeShopItemInfo

class RequestRecipeShopMakeInfo : L2GameClientPacket() {
    private var _objectId: Int = 0
    private var _recipeId: Int = 0

    override fun readImpl() {
        _objectId = readD()
        _recipeId = readD()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        val manufacturer = World.getInstance().getPlayer(_objectId)
        if (manufacturer == null || manufacturer.storeType != Player.StoreType.MANUFACTURE)
            return

        player.sendPacket(RecipeShopItemInfo(manufacturer, _recipeId))
    }
}