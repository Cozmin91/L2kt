package com.l2kt.gameserver.network.serverpackets;

import com.l2kt.gameserver.data.xml.RecipeData;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.item.Recipe;

public class RecipeItemMakeInfo extends L2GameServerPacket
{
	private final int _id;
	private final Player _activeChar;
	private final int _status;
	
	public RecipeItemMakeInfo(int id, Player player, int status)
	{
		_id = id;
		_activeChar = player;
		_status = status;
	}
	
	public RecipeItemMakeInfo(int id, Player player)
	{
		_id = id;
		_activeChar = player;
		_status = -1;
	}
	
	@Override
	protected final void writeImpl()
	{
		Recipe recipe = RecipeData.getInstance().getRecipeList(_id);
		if (recipe != null)
		{
			writeC(0xD7);
			
			writeD(_id);
			writeD((recipe.isDwarven()) ? 0 : 1);
			writeD((int) _activeChar.getCurrentMp());
			writeD(_activeChar.getMaxMp());
			writeD(_status);
		}
	}
}