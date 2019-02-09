package com.l2kt.gameserver.model.zone.type;

import com.l2kt.gameserver.model.zone.ZoneId;
import com.l2kt.gameserver.model.zone.ZoneType;
import com.l2kt.gameserver.model.actor.Creature;

/**
 * A zone extending {@link ZoneType}, used for quests and custom scripts.
 */
public class ScriptZone extends ZoneType
{
	public ScriptZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		character.setInsideZone(ZoneId.SCRIPT, true);
	}
	
	@Override
	protected void onExit(Creature character)
	{
		character.setInsideZone(ZoneId.SCRIPT, false);
	}
}