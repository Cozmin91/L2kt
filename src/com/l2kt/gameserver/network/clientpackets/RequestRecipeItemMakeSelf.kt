package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.xml.RecipeData
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.craft.RecipeItemMaker
import com.l2kt.gameserver.network.FloodProtectors
import com.l2kt.gameserver.network.SystemMessageId

class RequestRecipeItemMakeSelf : L2GameClientPacket() {
    private var _recipeId: Int = 0

    override fun readImpl() {
        _recipeId = readD()
    }

    override fun runImpl() {
        if (!FloodProtectors.performAction(client, FloodProtectors.Action.MANUFACTURE))
            return

        val player = client.activeChar ?: return

        if (player.storeType == Player.StoreType.MANUFACTURE || player.isCrafting)
            return

        if (player.isInDuel || player.isInCombat) {
            player.sendPacket(SystemMessageId.CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT)
            return
        }

        val recipe = RecipeData.getRecipeList(_recipeId) ?: return

        if (recipe.isDwarven) {
            if (!player.dwarvenRecipeBook.contains(recipe))
                return
        } else {
            if (!player.commonRecipeBook.contains(recipe))
                return
        }

        val maker = RecipeItemMaker(player, recipe, player)
        if (maker._isValid)
            maker.run()
    }
}