package com.l2kt.gameserver.model.zone.type;

import com.l2kt.gameserver.model.zone.ZoneId;
import com.l2kt.gameserver.model.zone.ZoneType;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.instance.Player;

/**
 * A zone extending {@link ZoneType}, used to restrict {@link Player}s to enter mounted on wyverns.<br>
 * <br>
 * A task and a message is called if event is triggered. If the player didn't leave after 5 seconds, he will be dismounted.
 */
public class NoLandingZone extends ZoneType
{
	public NoLandingZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		if (character instanceof Player)
		{
			character.setInsideZone(ZoneId.NO_LANDING, true);
			
			((Player) character).enterOnNoLandingZone();
		}
	}
	
	@Override
	protected void onExit(Creature character)
	{
		if (character instanceof Player)
		{
			character.setInsideZone(ZoneId.NO_LANDING, false);
			
			((Player) character).exitOnNoLandingZone();
		}
	}
}