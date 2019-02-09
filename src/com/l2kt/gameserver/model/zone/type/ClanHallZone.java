package com.l2kt.gameserver.model.zone.type;

import com.l2kt.gameserver.data.xml.MapRegionData;
import com.l2kt.gameserver.model.zone.SpawnZoneType;
import com.l2kt.gameserver.model.zone.ZoneId;
import com.l2kt.gameserver.instancemanager.ClanHallManager;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.entity.ClanHall;
import com.l2kt.gameserver.network.serverpackets.ClanHallDecoration;

/**
 * A zone extending {@link SpawnZoneType} used by {@link ClanHall}s.
 */
public class ClanHallZone extends SpawnZoneType
{
	private int _clanHallId;
	
	public ClanHallZone(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("clanHallId"))
		{
			_clanHallId = Integer.parseInt(value);
			
			// Register self to the correct clan hall
			ClanHallManager.getInstance().getClanHallById(_clanHallId).setZone(this);
		}
		else
			super.setParameter(name, value);
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		if (character instanceof Player)
		{
			// Set as in clan hall
			character.setInsideZone(ZoneId.CLAN_HALL, true);
			
			final ClanHall ch = ClanHallManager.getInstance().getClanHallById(_clanHallId);
			if (ch == null)
				return;
			
			// Send decoration packet
			character.sendPacket(new ClanHallDecoration(ch));
		}
	}
	
	@Override
	protected void onExit(Creature character)
	{
		if (character instanceof Player)
			character.setInsideZone(ZoneId.CLAN_HALL, false);
	}
	
	/**
	 * Kick {@link Player}s who don't belong to the clan set as parameter from this zone. They are ported to town.
	 * @param clanId : The clanhall owner id. Related players aren't teleported out.
	 */
	public void banishForeigners(int clanId)
	{
		if (_characters.isEmpty())
			return;
		
		for (Player player : getKnownTypeInside(Player.class))
		{
			if (player.getClanId() == clanId)
				continue;
			
			player.teleToLocation(MapRegionData.TeleportType.TOWN);
		}
	}
	
	public int getClanHallId()
	{
		return _clanHallId;
	}
}