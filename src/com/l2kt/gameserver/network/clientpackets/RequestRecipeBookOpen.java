package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.RecipeBookItemList;

public final class RequestRecipeBookOpen extends L2GameClientPacket
{
	private boolean _isDwarven;
	
	@Override
	protected void readImpl()
	{
		_isDwarven = (readD() == 0);
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (player.isCastingNow() || player.isAllSkillsDisabled())
		{
			player.sendPacket(SystemMessageId.NO_RECIPE_BOOK_WHILE_CASTING);
			return;
		}
		
		player.sendPacket(new RecipeBookItemList(player, _isDwarven));
	}
}