package com.l2kt.gameserver.data.manager;

import java.util.HashMap;
import java.util.Map;

import com.l2kt.gameserver.idfactory.IdFactory;
import com.l2kt.gameserver.model.World;
import com.l2kt.gameserver.model.actor.Boat;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.actor.template.CreatureTemplate;
import com.l2kt.gameserver.model.location.BoatLocation;
import com.l2kt.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2kt.gameserver.templates.StatsSet;

public class BoatManager
{
	public static final int TALKING_ISLAND = 0;
	public static final int GLUDIN_HARBOR = 1;
	public static final int RUNE_HARBOR = 2;
	
	public static final int BOAT_BROADCAST_RADIUS = 20000;
	
	private final Map<Integer, Boat> _boats = new HashMap<>();
	private final boolean[] _docksBusy = new boolean[3];
	
	protected BoatManager()
	{
	}
	
	/**
	 * Generate a new {@link Boat}, using a fresh {@link CreatureTemplate}.
	 * @param boatId : The boat id to use.
	 * @param x : The X position to use.
	 * @param y : The Y position to use.
	 * @param z : The Z position to use.
	 * @param heading : The heading to use.
	 * @return the new boat instance.
	 */
	public Boat getNewBoat(int boatId, int x, int y, int z, int heading)
	{
		final StatsSet set = new StatsSet();
		set.set("id", boatId);
		set.set("level", 0);
		
		set.set("str", 0);
		set.set("con", 0);
		set.set("dex", 0);
		set.set("int", 0);
		set.set("wit", 0);
		set.set("men", 0);
		
		set.set("hp", 50000);
		set.set("mp", 0);
		
		set.set("hpRegen", 3.e-3f);
		set.set("mpRegen", 3.e-3f);
		
		set.set("radius", 0);
		set.set("height", 0);
		set.set("type", "");
		
		set.set("exp", 0);
		set.set("sp", 0);
		
		set.set("pAtk", 0);
		set.set("mAtk", 0);
		set.set("pDef", 100);
		set.set("mDef", 100);
		
		set.set("rHand", 0);
		set.set("lHand", 0);
		
		set.set("walkSpd", 0);
		set.set("runSpd", 0);
		
		final CreatureTemplate template = new CreatureTemplate(set);
		final Boat boat = new Boat(IdFactory.getInstance().getNextId(), template);
		
		_boats.put(boat.getObjectId(), boat);
		
		boat.setHeading(heading);
		boat.spawnMe(x, y, z);
		
		return boat;
	}
	
	public Boat getBoat(int boatId)
	{
		return _boats.get(boatId);
	}
	
	/**
	 * Lock/unlock dock so only one boat can be docked.
	 * @param dockId : The dock id.
	 * @param value : True if the dock is locked.
	 */
	public void dockBoat(int dockId, boolean value)
	{
		_docksBusy[dockId] = value;
	}
	
	/**
	 * Check if the dock is busy.
	 * @param dockId : The dock id.
	 * @return true if the dock is locked, false otherwise.
	 */
	public boolean isBusyDock(int dockId)
	{
		return _docksBusy[dockId];
	}
	
	/**
	 * Broadcast one packet in both path points.
	 * @param point1 : The first location to broadcast the packet.
	 * @param point2 : The second location to broadcast the packet.
	 * @param packet : The packet to broadcast.
	 */
	public void broadcastPacket(BoatLocation point1, BoatLocation point2, L2GameServerPacket packet)
	{
		for (Player player : World.getInstance().getPlayers())
		{
			double dx = (double) player.getX() - point1.getX();
			double dy = (double) player.getY() - point1.getY();
			
			if (Math.sqrt(dx * dx + dy * dy) < BOAT_BROADCAST_RADIUS)
				player.sendPacket(packet);
			else
			{
				dx = (double) player.getX() - point2.getX();
				dy = (double) player.getY() - point2.getY();
				
				if (Math.sqrt(dx * dx + dy * dy) < BOAT_BROADCAST_RADIUS)
					player.sendPacket(packet);
			}
		}
	}
	
	/**
	 * Broadcast several packets in both path points.
	 * @param point1 : The first location to broadcast the packet.
	 * @param point2 : The second location to broadcast the packet.
	 * @param packets : The packets to broadcast.
	 */
	public void broadcastPackets(BoatLocation point1, BoatLocation point2, L2GameServerPacket... packets)
	{
		for (Player player : World.getInstance().getPlayers())
		{
			double dx = (double) player.getX() - point1.getX();
			double dy = (double) player.getY() - point1.getY();
			
			if (Math.sqrt(dx * dx + dy * dy) < BOAT_BROADCAST_RADIUS)
			{
				for (L2GameServerPacket p : packets)
					player.sendPacket(p);
			}
			else
			{
				dx = (double) player.getX() - point2.getX();
				dy = (double) player.getY() - point2.getY();
				
				if (Math.sqrt(dx * dx + dy * dy) < BOAT_BROADCAST_RADIUS)
					for (L2GameServerPacket p : packets)
						player.sendPacket(p);
			}
		}
	}
	
	public static final BoatManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final BoatManager INSTANCE = new BoatManager();
	}
}