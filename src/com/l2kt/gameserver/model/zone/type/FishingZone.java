package com.l2kt.gameserver.model.zone.type;

import com.l2kt.gameserver.model.zone.ZoneType;
import com.l2kt.gameserver.model.actor.Creature;

/**
 * A zone extending {@link ZoneType}, used for fish points.
 */
public class FishingZone extends ZoneType
{
	public FishingZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature character)
	{
	}
	
	@Override
	protected void onExit(Creature character)
	{
	}
	
	public int getWaterZ()
	{
		return getZone().getHighZ();
	}
}