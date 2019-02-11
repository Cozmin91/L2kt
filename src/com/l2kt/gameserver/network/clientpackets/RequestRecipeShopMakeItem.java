package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.commons.math.MathUtil;
import com.l2kt.gameserver.data.xml.RecipeData;
import com.l2kt.gameserver.model.World;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.craft.RecipeItemMaker;
import com.l2kt.gameserver.model.item.Recipe;
import com.l2kt.gameserver.network.FloodProtectors;
import com.l2kt.gameserver.network.SystemMessageId;

public final class RequestRecipeShopMakeItem extends L2GameClientPacket
{
	private int _objectId;
	private int _recipeId;
	@SuppressWarnings("unused")
	private int _unknow;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_recipeId = readD();
		_unknow = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (!FloodProtectors.INSTANCE.performAction(getClient(), FloodProtectors.Action.MANUFACTURE))
			return;
		
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		final Player manufacturer = World.getInstance().getPlayer(_objectId);
		if (manufacturer == null)
			return;
		
		if (player.isInStoreMode())
			return;
		
		if (manufacturer.getStoreType() != Player.StoreType.MANUFACTURE)
			return;
		
		if (player.isCrafting() || manufacturer.isCrafting())
			return;
		
		if (manufacturer.isInDuel() || player.isInDuel() || manufacturer.isInCombat() || player.isInCombat())
		{
			player.sendPacket(SystemMessageId.CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT);
			return;
		}
		
		if (!MathUtil.INSTANCE.checkIfInRange(150, player, manufacturer, true))
			return;
		
		final Recipe recipe = RecipeData.getInstance().getRecipeList(_recipeId);
		if (recipe == null)
			return;
		
		if (recipe.isDwarven())
		{
			if (!manufacturer.getDwarvenRecipeBook().contains(recipe))
				return;
		}
		else
		{
			if (!manufacturer.getCommonRecipeBook().contains(recipe))
				return;
		}
		
		final RecipeItemMaker maker = new RecipeItemMaker(manufacturer, recipe, player);
		if (maker._isValid)
			maker.run();
	}
}