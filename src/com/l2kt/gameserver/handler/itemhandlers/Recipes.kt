package com.l2kt.gameserver.handler.itemhandlers

import com.l2kt.Config
import com.l2kt.gameserver.data.xml.RecipeData
import com.l2kt.gameserver.handler.IItemHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.RecipeBookItemList
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class Recipes : IItemHandler {
    override fun useItem(playable: Playable, item: ItemInstance, forceUse: Boolean) {
        if (playable !is Player)
            return

        if (!Config.IS_CRAFTING_ENABLED) {
            playable.sendMessage("Crafting is disabled, you cannot register this recipe.")
            return
        }

        if (playable.isCrafting) {
            playable.sendPacket(SystemMessageId.CANT_ALTER_RECIPEBOOK_WHILE_CRAFTING)
            return
        }

        val recipe = RecipeData.getRecipeByItemId(item.itemId) ?: return

        if (playable.hasRecipeList(recipe.id)) {
            playable.sendPacket(SystemMessageId.RECIPE_ALREADY_REGISTERED)
            return
        }

        if (recipe.isDwarven) {
            if (!playable.hasDwarvenCraft())
                playable.sendPacket(SystemMessageId.CANT_REGISTER_NO_ABILITY_TO_CRAFT)
            else if (playable.storeType == Player.StoreType.MANUFACTURE)
                playable.sendPacket(SystemMessageId.CANT_ALTER_RECIPEBOOK_WHILE_CRAFTING)
            else if (recipe.level > playable.getSkillLevel(L2Skill.SKILL_CREATE_DWARVEN))
                playable.sendPacket(SystemMessageId.CREATE_LVL_TOO_LOW_TO_REGISTER)
            else if (playable.dwarvenRecipeBook.size >= playable.dwarfRecipeLimit)
                playable.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.UP_TO_S1_RECIPES_CAN_REGISTER).addNumber(
                        playable.dwarfRecipeLimit
                    )
                )
            else if (playable.destroyItem("Consume", item.objectId, 1, null, false)) {
                playable.registerDwarvenRecipeList(recipe)
                playable.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ADDED).addItemName(item))
                playable.sendPacket(RecipeBookItemList(playable, true))
            }
        } else {
            if (!playable.hasCommonCraft())
                playable.sendPacket(SystemMessageId.CANT_REGISTER_NO_ABILITY_TO_CRAFT)
            else if (playable.storeType == Player.StoreType.MANUFACTURE)
                playable.sendPacket(SystemMessageId.CANT_ALTER_RECIPEBOOK_WHILE_CRAFTING)
            else if (recipe.level > playable.getSkillLevel(L2Skill.SKILL_CREATE_COMMON))
                playable.sendPacket(SystemMessageId.CREATE_LVL_TOO_LOW_TO_REGISTER)
            else if (playable.commonRecipeBook.size >= playable.commonRecipeLimit)
                playable.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.UP_TO_S1_RECIPES_CAN_REGISTER).addNumber(
                        playable.commonRecipeLimit
                    )
                )
            else if (playable.destroyItem("Consume", item.objectId, 1, null, false)) {
                playable.registerCommonRecipeList(recipe)
                playable.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ADDED).addItemName(item))
                playable.sendPacket(RecipeBookItemList(playable, false))
            }
        }
    }
}