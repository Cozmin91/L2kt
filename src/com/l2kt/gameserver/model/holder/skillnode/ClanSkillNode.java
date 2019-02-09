package com.l2kt.gameserver.model.holder.skillnode;

import com.l2kt.gameserver.templates.StatsSet;

/**
 * A datatype used by clan skill types. It extends {@link GeneralSkillNode}.
 */
public final class ClanSkillNode extends GeneralSkillNode
{
	private final int _itemId;
	
	public ClanSkillNode(StatsSet set)
	{
		super(set);
		
		_itemId = set.getInteger("itemId");
	}
	
	public int getItemId()
	{
		return _itemId;
	}
}