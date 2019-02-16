package com.l2kt.gameserver.model.olympiad;

import com.l2kt.gameserver.model.World;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.templates.StatsSet;

/**
 * @author DS
 */
public final class Participant
{
	public final int objectId;
	public Player player;
	public final String name;
	public final int side;
	public final int baseClass;
	public boolean disconnected = false;
	public boolean defaulted = false;
	public final StatsSet stats;
	
	public Participant(Player plr, int olympiadSide)
	{
		objectId = plr.getObjectId();
		player = plr;
		name = plr.getName();
		side = olympiadSide;
		baseClass = plr.getBaseClass();
		stats = Olympiad.getNobleStats(objectId);
	}
	
	public Participant(int objId, int olympiadSide)
	{
		objectId = objId;
		player = null;
		name = "-";
		side = olympiadSide;
		baseClass = 0;
		stats = null;
	}
	
	public final void updatePlayer()
	{
		if (player == null || !player.isOnline())
			player = World.INSTANCE.getPlayer(objectId);
	}
	
	public final void updateStat(String statName, int increment)
	{
		stats.set(statName, Math.max(stats.getInteger(statName) + increment, 0));
	}
}