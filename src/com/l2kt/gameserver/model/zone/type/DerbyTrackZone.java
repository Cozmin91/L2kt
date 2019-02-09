package com.l2kt.gameserver.model.zone.type;

import com.l2kt.gameserver.model.zone.ZoneId;
import com.l2kt.gameserver.model.zone.ZoneType;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.Playable;

/**
 * A zone extending {@link ZoneType} used by Derby Track system.<br>
 * <br>
 * The zone shares peace, no summon and monster track behaviors.
 */
public class DerbyTrackZone extends ZoneType
{
	public DerbyTrackZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		if (character instanceof Playable)
		{
			character.setInsideZone(ZoneId.MONSTER_TRACK, true);
			character.setInsideZone(ZoneId.PEACE, true);
			character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
		}
	}
	
	@Override
	protected void onExit(Creature character)
	{
		if (character instanceof Playable)
		{
			character.setInsideZone(ZoneId.MONSTER_TRACK, false);
			character.setInsideZone(ZoneId.PEACE, false);
			character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
		}
	}
}