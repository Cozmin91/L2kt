package com.l2kt.gameserver.model.zone.type;

import com.l2kt.commons.random.Rnd;
import com.l2kt.gameserver.model.zone.ZoneId;
import com.l2kt.gameserver.model.zone.ZoneType;

import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.instance.Player;

/**
 * A zone extending {@link ZoneType} used for Mass Gatekeepers to teleport players on a specific location.<br>
 * <br>
 * Summoning is forbidden. It holds a location under an int array, and castleId.
 */
public class CastleTeleportZone extends ZoneType
{
	private final int[] _spawnLoc;
	private int _castleId;
	
	public CastleTeleportZone(int id)
	{
		super(id);
		
		_spawnLoc = new int[5];
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("castleId"))
			_castleId = Integer.parseInt(value);
		else if (name.equals("spawnMinX"))
			_spawnLoc[0] = Integer.parseInt(value);
		else if (name.equals("spawnMaxX"))
			_spawnLoc[1] = Integer.parseInt(value);
		else if (name.equals("spawnMinY"))
			_spawnLoc[2] = Integer.parseInt(value);
		else if (name.equals("spawnMaxY"))
			_spawnLoc[3] = Integer.parseInt(value);
		else if (name.equals("spawnZ"))
			_spawnLoc[4] = Integer.parseInt(value);
		else
			super.setParameter(name, value);
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
	}
	
	@Override
	protected void onExit(Creature character)
	{
		character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
	}
	
	public void oustAllPlayers()
	{
		if (_characters.isEmpty())
			return;
		
		for (Player player : getKnownTypeInside(Player.class))
			player.teleToLocation(Rnd.INSTANCE.get(_spawnLoc[0], _spawnLoc[1]), Rnd.INSTANCE.get(_spawnLoc[2], _spawnLoc[3]), _spawnLoc[4], 0);
	}
	
	public int getCastleId()
	{
		return _castleId;
	}
}