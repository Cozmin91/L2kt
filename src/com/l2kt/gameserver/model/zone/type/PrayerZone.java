package com.l2kt.gameserver.model.zone.type;

import com.l2kt.gameserver.model.zone.ZoneId;
import com.l2kt.gameserver.model.zone.ZoneType;
import com.l2kt.gameserver.model.actor.Creature;

/**
 * A zone extending {@link ZoneType}, used for castle's artifacts.<br>
 * <br>
 * A check forces players to cast on this type of zone, to avoid hiding spots or exploits.
 */
public class PrayerZone extends ZoneType
{
	public PrayerZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		character.setInsideZone(ZoneId.CAST_ON_ARTIFACT, true);
	}
	
	@Override
	protected void onExit(Creature character)
	{
		character.setInsideZone(ZoneId.CAST_ON_ARTIFACT, false);
	}
}