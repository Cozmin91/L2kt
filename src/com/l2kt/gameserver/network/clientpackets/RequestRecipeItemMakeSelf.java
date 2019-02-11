package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.gameserver.data.xml.RecipeData;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.craft.RecipeItemMaker;
import com.l2kt.gameserver.model.item.Recipe;
import com.l2kt.gameserver.network.FloodProtectors;
import com.l2kt.gameserver.network.SystemMessageId;

public final class RequestRecipeItemMakeSelf extends L2GameClientPacket
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
		if (!FloodProtectors.INSTANCE.performAction(getClient(), FloodProtectors.Action.MANUFACTURE))
			return;
		
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (player.getStoreType() == Player.StoreType.MANUFACTURE || player.isCrafting())
			return;
		
		if (player.isInDuel() || player.isInCombat())
		{
			player.sendPacket(SystemMessageId.CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT);
			return;
		}
		
		final Recipe recipe = RecipeData.getInstance().getRecipeList(_recipeId);
		if (recipe == null)
			return;
		
		if (recipe.isDwarven())
		{
			if (!player.getDwarvenRecipeBook().contains(recipe))
				return;
		}
		else
		{
			if (!player.getCommonRecipeBook().contains(recipe))
				return;
		}
		
		final RecipeItemMaker maker = new RecipeItemMaker(player, recipe, player);
		if (maker._isValid)
			maker.run();
	}
}