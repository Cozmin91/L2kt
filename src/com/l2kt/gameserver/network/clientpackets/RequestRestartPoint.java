package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.Config;
import com.l2kt.commons.concurrent.ThreadPool;
import com.l2kt.gameserver.data.manager.CastleManager;
import com.l2kt.gameserver.data.xml.MapRegionData;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.entity.ClanHall;
import com.l2kt.gameserver.model.entity.Siege;
import com.l2kt.gameserver.model.location.Location;
import com.l2kt.gameserver.model.pledge.Clan;

import com.l2kt.gameserver.instancemanager.ClanHallManager;

public final class RequestRestartPoint extends L2GameClientPacket
{
	protected static final Location JAIL_LOCATION = new Location(-114356, -249645, -2984);
	
	protected int _requestType;
	
	@Override
	protected void readImpl()
	{
		_requestType = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (player.isFakeDeath())
		{
			player.stopFakeDeath(true);
			return;
		}
		
		if (!player.isDead())
			return;
		
		// Schedule a respawn delay if player is part of a clan registered in an active siege.
		if (player.getClan() != null)
		{
			final Siege siege = CastleManager.getInstance().getActiveSiege(player);
			if (siege != null && siege.checkSide(player.getClan(), Siege.SiegeSide.ATTACKER))
			{
				ThreadPool.schedule(() -> portPlayer(player), Config.ATTACKERS_RESPAWN_DELAY);
				return;
			}
		}
		
		portPlayer(player);
	}
	
	/**
	 * Teleport the {@link Player} to the associated {@link Location}, based on _requestType.
	 * @param player : The player set as parameter.
	 */
	private void portPlayer(Player player)
	{
		final Clan clan = player.getClan();
		
		Location loc = null;
		
		// Enforce type.
		if (player.isInJail())
			_requestType = 27;
		else if (player.isFestivalParticipant())
			_requestType = 4;
		
		// To clanhall.
		if (_requestType == 1)
		{
			if (clan == null || !clan.hasHideout())
				return;
			
			loc = MapRegionData.getInstance().getLocationToTeleport(player, MapRegionData.TeleportType.CLAN_HALL);
			
			final ClanHall ch = ClanHallManager.getInstance().getClanHallByOwner(clan);
			if (ch != null)
			{
				final ClanHall.ClanHallFunction function = ch.getFunction(ClanHall.FUNC_RESTORE_EXP);
				if (function != null)
					player.restoreExp(function.getLvl());
			}
		}
		// To castle.
		else if (_requestType == 2)
		{
			final Siege siege = CastleManager.getInstance().getActiveSiege(player);
			if (siege != null)
			{
				final Siege.SiegeSide side = siege.getSide(clan);
				if (side == Siege.SiegeSide.DEFENDER || side == Siege.SiegeSide.OWNER)
					loc = MapRegionData.getInstance().getLocationToTeleport(player, MapRegionData.TeleportType.CASTLE);
				else if (side == Siege.SiegeSide.ATTACKER)
					loc = MapRegionData.getInstance().getLocationToTeleport(player, MapRegionData.TeleportType.TOWN);
				else
					return;
			}
			else
			{
				if (clan == null || !clan.hasCastle())
					return;
				
				loc = MapRegionData.getInstance().getLocationToTeleport(player, MapRegionData.TeleportType.CASTLE);
			}
		}
		// To siege flag.
		else if (_requestType == 3)
			loc = MapRegionData.getInstance().getLocationToTeleport(player, MapRegionData.TeleportType.SIEGE_FLAG);
		// Fixed.
		else if (_requestType == 4)
		{
			if (!player.isGM() && !player.isFestivalParticipant())
				return;
			
			loc = player.getPosition();
		}
		// To jail.
		else if (_requestType == 27)
		{
			if (!player.isInJail())
				return;
			
			loc = JAIL_LOCATION;
		}
		// Nothing has been found, use regular "To town" behavior.
		else
			loc = MapRegionData.getInstance().getLocationToTeleport(player, MapRegionData.TeleportType.TOWN);
		
		player.setIsIn7sDungeon(false);
		
		if (player.isDead())
			player.doRevive();
		
		player.teleToLocation(loc, 20);
	}
}