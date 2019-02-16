package com.l2kt.gameserver.network.clientpackets

import com.l2kt.commons.math.MathUtil
import com.l2kt.gameserver.data.xml.RecipeData
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.craft.RecipeItemMaker
import com.l2kt.gameserver.network.FloodProtectors
import com.l2kt.gameserver.network.SystemMessageId

class RequestRecipeShopMakeItem : L2GameClientPacket() {
    private var _objectId: Int = 0
    private var _recipeId: Int = 0
    private var _unknow: Int = 0

    override fun readImpl() {
        _objectId = readD()
        _recipeId = readD()
        _unknow = readD()
    }

    override fun runImpl() {
        if (!FloodProtectors.performAction(client, FloodProtectors.Action.MANUFACTURE))
            return

        val player = client.activeChar ?: return

        val manufacturer = World.getPlayer(_objectId) ?: return

        if (player.isInStoreMode)
            return

        if (manufacturer.storeType != Player.StoreType.MANUFACTURE)
            return

        if (player.isCrafting || manufacturer.isCrafting)
            return

        if (manufacturer.isInDuel || player.isInDuel || manufacturer.isInCombat || player.isInCombat) {
            player.sendPacket(SystemMessageId.CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT)
            return
        }

        if (!MathUtil.checkIfInRange(150, player, manufacturer, true))
            return

        val recipe = RecipeData.getRecipeList(_recipeId) ?: return

        if (recipe.isDwarven) {
            if (!manufacturer.dwarvenRecipeBook.contains(recipe))
                return
        } else {
            if (!manufacturer.commonRecipeBook.contains(recipe))
                return
        }

        val maker = RecipeItemMaker(manufacturer, recipe, player)
        if (maker._isValid)
            maker.run()
    }
}