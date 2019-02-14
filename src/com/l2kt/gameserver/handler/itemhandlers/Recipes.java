package com.l2kt.gameserver.handler.itemhandlers;

import com.l2kt.Config;
import com.l2kt.gameserver.data.xml.RecipeData;
import com.l2kt.gameserver.handler.IItemHandler;
import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.actor.Playable;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.item.Recipe;
import com.l2kt.gameserver.model.item.instance.ItemInstance;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.RecipeBookItemList;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;

public class Recipes implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
		
		final Player player = (Player) playable;
		
		if (!Config.IS_CRAFTING_ENABLED)
		{
			player.sendMessage("Crafting is disabled, you cannot register this recipe.");
			return;
		}
		
		if (player.isCrafting())
		{
			player.sendPacket(SystemMessageId.CANT_ALTER_RECIPEBOOK_WHILE_CRAFTING);
			return;
		}
		
		final Recipe recipe = RecipeData.INSTANCE.getRecipeByItemId(item.getItemId());
		if (recipe == null)
			return;
		
		if (player.hasRecipeList(recipe.getId()))
		{
			player.sendPacket(SystemMessageId.RECIPE_ALREADY_REGISTERED);
			return;
		}
		
		if (recipe.isDwarven())
		{
			if (!player.hasDwarvenCraft())
				player.sendPacket(SystemMessageId.CANT_REGISTER_NO_ABILITY_TO_CRAFT);
			else if (player.getStoreType() == Player.StoreType.MANUFACTURE)
				player.sendPacket(SystemMessageId.CANT_ALTER_RECIPEBOOK_WHILE_CRAFTING);
			else if (recipe.getLevel() > player.getSkillLevel(L2Skill.SKILL_CREATE_DWARVEN))
				player.sendPacket(SystemMessageId.CREATE_LVL_TOO_LOW_TO_REGISTER);
			else if (player.getDwarvenRecipeBook().size() >= player.getDwarfRecipeLimit())
				player.sendPacket(SystemMessage.Companion.getSystemMessage(SystemMessageId.UP_TO_S1_RECIPES_CAN_REGISTER).addNumber(player.getDwarfRecipeLimit()));
			else if (player.destroyItem("Consume", item.getObjectId(), 1, null, false))
			{
				player.registerDwarvenRecipeList(recipe);
				player.sendPacket(SystemMessage.Companion.getSystemMessage(SystemMessageId.S1_ADDED).addItemName(item));
				player.sendPacket(new RecipeBookItemList(player, true));
			}
		}
		else
		{
			if (!player.hasCommonCraft())
				player.sendPacket(SystemMessageId.CANT_REGISTER_NO_ABILITY_TO_CRAFT);
			else if (player.getStoreType() == Player.StoreType.MANUFACTURE)
				player.sendPacket(SystemMessageId.CANT_ALTER_RECIPEBOOK_WHILE_CRAFTING);
			else if (recipe.getLevel() > player.getSkillLevel(L2Skill.SKILL_CREATE_COMMON))
				player.sendPacket(SystemMessageId.CREATE_LVL_TOO_LOW_TO_REGISTER);
			else if (player.getCommonRecipeBook().size() >= player.getCommonRecipeLimit())
				player.sendPacket(SystemMessage.Companion.getSystemMessage(SystemMessageId.UP_TO_S1_RECIPES_CAN_REGISTER).addNumber(player.getCommonRecipeLimit()));
			else if (player.destroyItem("Consume", item.getObjectId(), 1, null, false))
			{
				player.registerCommonRecipeList(recipe);
				player.sendPacket(SystemMessage.Companion.getSystemMessage(SystemMessageId.S1_ADDED).addItemName(item));
				player.sendPacket(new RecipeBookItemList(player, false));
			}
		}
	}
}