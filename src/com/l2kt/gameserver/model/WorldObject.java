package com.l2kt.gameserver.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.l2kt.Config;
import com.l2kt.commons.logging.CLogger;
import com.l2kt.commons.math.MathUtil;
import com.l2kt.gameserver.data.ItemTable;
import com.l2kt.gameserver.data.xml.NpcData;

import com.l2kt.gameserver.idfactory.IdFactory;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.Npc;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.actor.template.NpcTemplate;
import com.l2kt.gameserver.model.location.Location;
import com.l2kt.gameserver.model.location.SpawnLocation;
import com.l2kt.gameserver.model.zone.ZoneId;
import com.l2kt.gameserver.network.serverpackets.ActionFailed;
import com.l2kt.gameserver.taskmanager.DebugMovementTaskManager;

/**
 * Mother class of all interactive objects in the world (PC, NPC, Item...)
 */
public abstract class WorldObject
{
	public enum PolyType
	{
		ITEM,
		NPC,
		DEFAULT;
	}
	
	public static final CLogger LOGGER = new CLogger(WorldObject.class.getName());
	
	private String _name;
	private int _objectId;
	
	private NpcTemplate _polyTemplate;
	private PolyType _polyType = PolyType.DEFAULT;
	private int _polyId;
	
	private SpawnLocation _position = new SpawnLocation(0, 0, 0, 0);
	private WorldRegion _region;
	
	private boolean _isVisible;
	
	public WorldObject(int objectId)
	{
		_objectId = objectId;
	}
	
	/**
	 * @param attacker : The target to make checks on.
	 * @return true if this {@link WorldObject} is attackable or false if it isn't.
	 */
	public abstract boolean isAutoAttackable(Creature attacker);
	
	@Override
	public String toString()
	{
		return (getClass().getSimpleName() + ":" + getName() + "[" + getObjectId() + "]");
	}
	
	public void onAction(Player player)
	{
		player.sendPacket(ActionFailed.Companion.getSTATIC_PACKET());
	}
	
	public void onActionShift(Player player)
	{
		player.sendPacket(ActionFailed.Companion.getSTATIC_PACKET());
	}
	
	public void onForcedAttack(Player player)
	{
		player.sendPacket(ActionFailed.Companion.getSTATIC_PACKET());
	}
	
	public void onSpawn()
	{
	}
	
	/**
	 * Remove this {@link WorldObject} from the world.
	 */
	public void decayMe()
	{
		setRegion(null);
		
		World.getInstance().removeObject(this);
	}
	
	public void refreshID()
	{
		World.getInstance().removeObject(this);
		IdFactory.getInstance().releaseId(getObjectId());
		_objectId = IdFactory.getInstance().getNextId();
	}
	
	/**
	 * Spawn this {@link WorldObject} and add it in the world as a visible object.
	 */
	public final void spawnMe()
	{
		_isVisible = true;
		
		setRegion(World.getInstance().getRegion(_position));
		
		World.getInstance().addObject(this);
		
		onSpawn();
	}
	
	/**
	 * Initialize the position of this {@link WorldObject} and add it in the world as a visible object.
	 * @param loc : The location used as reference X/Y/Z.
	 */
	public final void spawnMe(Location loc)
	{
		spawnMe(loc.getX(), loc.getY(), loc.getZ());
	}
	
	/**
	 * Initialize the position of this {@link WorldObject} and add it in the world as a visible object.
	 * @param x : The X position to set.
	 * @param y : The Y position to set.
	 * @param z : The Z position to set.
	 */
	public final void spawnMe(int x, int y, int z)
	{
		_position.set(MathUtil.INSTANCE.limit(x, World.WORLD_X_MIN + 100, World.WORLD_X_MAX - 100), MathUtil.INSTANCE.limit(y, World.WORLD_Y_MIN + 100, World.WORLD_Y_MAX - 100), z);
		
		spawnMe();
	}
	
	public boolean isAttackable()
	{
		return false;
	}
	
	/**
	 * @return the visibilty state of this {@link WorldObject}.
	 */
	public final boolean isVisible()
	{
		return _region != null && _isVisible;
	}
	
	public final void setIsVisible(boolean value)
	{
		_isVisible = value;
		
		if (!_isVisible)
			setRegion(null);
	}
	
	public final String getName()
	{
		return _name;
	}
	
	public void setName(String value)
	{
		_name = value;
	}
	
	public final int getObjectId()
	{
		return _objectId;
	}
	
	public final NpcTemplate getPolyTemplate()
	{
		return _polyTemplate;
	}
	
	public final PolyType getPolyType()
	{
		return _polyType;
	}
	
	public final int getPolyId()
	{
		return _polyId;
	}
	
	public boolean polymorph(PolyType type, int id)
	{
		if (!(this instanceof Npc) && !(this instanceof Player))
			return false;
		
		if (type == PolyType.NPC)
		{
			final NpcTemplate template = NpcData.INSTANCE.getTemplate(id);
			if (template == null)
				return false;
			
			_polyTemplate = template;
		}
		else if (type == PolyType.ITEM)
		{
			if (ItemTable.INSTANCE.getTemplate(id) == null)
				return false;
		}
		else if (type == PolyType.DEFAULT)
			return false;
		
		_polyType = type;
		_polyId = id;
		
		decayMe();
		spawnMe();
		
		return true;
	}
	
	public void unpolymorph()
	{
		_polyTemplate = null;
		_polyType = PolyType.DEFAULT;
		_polyId = 0;
		
		decayMe();
		spawnMe();
	}
	
	public Player getActingPlayer()
	{
		return null;
	}
	
	/**
	 * Sends the Server->Client info packet for this {@link WorldObject}.
	 * @param player : The packet receiver.
	 */
	public void sendInfo(Player player)
	{
		
	}
	
	/**
	 * Check if this {@link WorldObject} has charged shot.
	 * @param type : The type of the shot to be checked.
	 * @return true if the object has charged shot.
	 */
	public boolean isChargedShot(ShotType type)
	{
		return false;
	}
	
	/**
	 * Charging shot into this {@link WorldObject}.
	 * @param type : The type of the shot to be (un)charged.
	 * @param charged : true if we charge, false if we uncharge.
	 */
	public void setChargedShot(ShotType type, boolean charged)
	{
	}
	
	/**
	 * Try to recharge a shot.
	 * @param physical : The skill is using Soulshots.
	 * @param magical : The skill is using Spiritshots.
	 */
	public void rechargeShots(boolean physical, boolean magical)
	{
	}
	
	/**
	 * Check if this {@link WorldObject} is in the given {@link ZoneId}.
	 * @param zone : The ZoneId to check.
	 * @return true if the object is in that ZoneId.
	 */
	public boolean isInsideZone(ZoneId zone)
	{
		return false;
	}
	
	/**
	 * Set the position of this {@link WorldObject} and if necessary modify its _region.
	 * @param x : The X position to set.
	 * @param y : The Y position to set.
	 * @param z : The Z position to set.
	 */
	public final void setXYZ(int x, int y, int z)
	{
		_position.set(x, y, z);
		
		if (Config.DEBUG_MOVEMENT > 0)
			DebugMovementTaskManager.INSTANCE.addItem(this, x, y, z);
		
		if (!isVisible())
			return;
		
		final WorldRegion region = World.getInstance().getRegion(_position);
		if (region != _region)
			setRegion(region);
	}
	
	/**
	 * Set the position of this {@link WorldObject} and make it invisible.
	 * @param x : The X position to set.
	 * @param y : The Y position to set.
	 * @param z : The Z position to set.
	 */
	public final void setXYZInvisible(int x, int y, int z)
	{
		_position.set(MathUtil.INSTANCE.limit(x, World.WORLD_X_MIN + 100, World.WORLD_X_MAX - 100), MathUtil.INSTANCE.limit(y, World.WORLD_Y_MIN + 100, World.WORLD_Y_MAX - 100), z);
		
		setIsVisible(false);
	}
	
	public final void setXYZInvisible(Location loc)
	{
		setXYZInvisible(loc.getX(), loc.getY(), loc.getZ());
	}
	
	public final int getX()
	{
		return _position.getX();
	}
	
	public final int getY()
	{
		return _position.getY();
	}
	
	public final int getZ()
	{
		return _position.getZ();
	}
	
	public final SpawnLocation getPosition()
	{
		return _position;
	}
	
	public final WorldRegion getRegion()
	{
		return _region;
	}
	
	/**
	 * Update current and surrounding {@link WorldRegion}s, based on both current region and region setted as parameter.
	 * @param newRegion : null to remove the {@link WorldObject}, or the new region.
	 */
	public void setRegion(WorldRegion newRegion)
	{
		List<WorldRegion> oldAreas = Collections.emptyList();
		
		if (_region != null)
		{
			_region.removeVisibleObject(this);
			oldAreas = _region.getSurroundingRegions();
		}
		
		List<WorldRegion> newAreas = Collections.emptyList();
		
		if (newRegion != null)
		{
			newRegion.addVisibleObject(this);
			newAreas = newRegion.getSurroundingRegions();
		}
		
		// For every old surrounding area NOT SHARED with new surrounding areas.
		for (WorldRegion region : oldAreas)
		{
			if (!newAreas.contains(region))
			{
				// Update all objects.
				for (WorldObject obj : region.getObjects())
				{
					if (obj == this)
						continue;
					
					obj.removeKnownObject(this);
					removeKnownObject(obj);
				}
				
				// Desactivate the old neighbor region.
				if (this instanceof Player && region.isEmptyNeighborhood())
					region.setActive(false);
			}
		}
		
		// For every new surrounding area NOT SHARED with old surrounding areas.
		for (WorldRegion region : newAreas)
		{
			if (!oldAreas.contains(region))
			{
				// Update all objects.
				for (WorldObject obj : region.getObjects())
				{
					if (obj == this)
						continue;
					
					obj.addKnownObject(this);
					addKnownObject(obj);
				}
				
				// Activate the new neighbor region.
				if (this instanceof Player)
					region.setActive(true);
			}
		}
		
		_region = newRegion;
	}
	
	/**
	 * Add a {@link WorldObject} to knownlist.
	 * @param object : An object to be added.
	 */
	public void addKnownObject(WorldObject object)
	{
	}
	
	/**
	 * Remove a {@link WorldObject} from knownlist.
	 * @param object : An object to be removed.
	 */
	public void removeKnownObject(WorldObject object)
	{
	}
	
	/**
	 * Return the knownlist of this {@link WorldObject} for a given object type.
	 * @param <A> : The object type must be an instance of WorldObject.
	 * @param type : The class specifying object type.
	 * @return List<A> : The knownlist of given object type.
	 */
	@SuppressWarnings("unchecked")
	public final <A> List<A> getKnownType(Class<A> type)
	{
		final WorldRegion region = _region;
		if (region == null)
			return Collections.emptyList();
		
		final List<A> result = new ArrayList<>();
		
		for (WorldRegion reg : region.getSurroundingRegions())
		{
			for (WorldObject obj : reg.getObjects())
			{
				if (obj == this || !type.isAssignableFrom(obj.getClass()))
					continue;
				
				result.add((A) obj);
			}
		}
		
		return result;
	}
	
	/**
	 * Return the knownlist of this {@link WorldObject} for a given object type within specified radius.
	 * @param <A> : The object type must be an instance of WorldObject.
	 * @param type : The class specifying object type.
	 * @param radius : The radius to check in which object must be located.
	 * @return List<A> : The knownlist of given object type.
	 */
	@SuppressWarnings("unchecked")
	public final <A> List<A> getKnownTypeInRadius(Class<A> type, int radius)
	{
		final WorldRegion region = _region;
		if (region == null)
			return Collections.emptyList();
		
		final List<A> result = new ArrayList<>();
		
		for (WorldRegion reg : region.getSurroundingRegions())
		{
			for (WorldObject obj : reg.getObjects())
			{
				if (obj == this || !type.isAssignableFrom(obj.getClass()) || !MathUtil.INSTANCE.checkIfInRange(radius, this, obj, true))
					continue;
				
				result.add((A) obj);
			}
		}
		
		return result;
	}
}