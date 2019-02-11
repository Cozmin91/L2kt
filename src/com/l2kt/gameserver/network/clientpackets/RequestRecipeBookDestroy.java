package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.gameserver.data.xml.RecipeData;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.item.Recipe;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.RecipeBookItemList;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;

public final class RequestRecipeBookDestroy extends L2GameClientPacket
{
	private int _recipeId;
	
	@Override
	protected void readImpl()
	{
		_recipeId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (player.getStoreType() == Player.StoreType.MANUFACTURE)
		{
			player.sendPacket(SystemMessageId.CANT_ALTER_RECIPEBOOK_WHILE_CRAFTING);
			return;
		}
		
		final Recipe recipe = RecipeData.getInstance().getRecipeList(_recipeId);
		if (recipe == null)
			return;
		
		player.unregisterRecipeList(_recipeId);
		player.sendPacket(SystemMessage.Companion.getSystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED).addItemName(recipe.getRecipeId()));
		player.sendPacket(new RecipeBookItemList(player, recipe.isDwarven()));
	}
}