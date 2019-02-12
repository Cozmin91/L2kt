package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.xml.RecipeData
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.RecipeBookItemList
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestRecipeBookDestroy : L2GameClientPacket() {
    private var _recipeId: Int = 0

    override fun readImpl() {
        _recipeId = readD()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        if (player.storeType == Player.StoreType.MANUFACTURE) {
            player.sendPacket(SystemMessageId.CANT_ALTER_RECIPEBOOK_WHILE_CRAFTING)
            return
        }

        val recipe = RecipeData.getInstance().getRecipeList(_recipeId) ?: return

        player.unregisterRecipeList(_recipeId)
        player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED).addItemName(recipe.recipeId))
        player.sendPacket(RecipeBookItemList(player, recipe.isDwarven))
    }
}