package com.l2kt.gameserver.model.actor.instance;

import com.l2kt.commons.random.Rnd;
import com.l2kt.gameserver.model.MinionList;
import com.l2kt.gameserver.model.actor.Attackable;
import com.l2kt.gameserver.model.actor.Creature;

import com.l2kt.gameserver.model.actor.template.NpcTemplate;

/**
 * A monster extends {@link Attackable} class.<br>
 * <br>
 * It is an attackable {@link Creature}, with the capability to hold minions/master.
 */
public class Monster extends Attackable
{
	private Monster _master;
	private MinionList _minionList;
	
	public Monster(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		// FIXME: to test to allow monsters hit others monsters
		if (attacker instanceof Monster)
			return false;
		
		return true;
	}
	
	@Override
	public boolean isAggressive()
	{
		return getTemplate().getAggroRange() > 0;
	}
	
	@Override
	public void onSpawn()
	{
		// Generate minions and spawn them (initial call and regular minions respawn are handled in the same method).
		if (!getTemplate().getMinionData().isEmpty())
			getMinionList().spawnMinions();
		
		super.onSpawn();
	}
	
	@Override
	public void onTeleported()
	{
		super.onTeleported();
		
		if (hasMinions())
			getMinionList().onMasterTeleported();
	}
	
	@Override
	public void deleteMe()
	{
		if (hasMinions())
			getMinionList().onMasterDeletion();
		else if (_master != null)
			_master.getMinionList().onMinionDeletion(this);
		
		super.deleteMe();
	}
	
	@Override
	public Monster getMaster()
	{
		return _master;
	}
	
	public void setMaster(Monster master)
	{
		_master = master;
	}
	
	public boolean hasMinions()
	{
		return _minionList != null;
	}
	
	public MinionList getMinionList()
	{
		if (_minionList == null)
			_minionList = new MinionList(this);
		
		return _minionList;
	}
	
	/**
	 * Teleport this {@link Monster} to its master.
	 */
	public void teleToMaster()
	{
		if (_master == null)
			return;
		
		// Init the position of the Minion and add it in the world as a visible object
		final int offset = (int) (100 + getCollisionRadius() + _master.getCollisionRadius());
		final int minRadius = (int) (_master.getCollisionRadius() + 30);
		
		int newX = Rnd.get(minRadius * 2, offset * 2); // x
		int newY = Rnd.get(newX, offset * 2); // distance
		newY = (int) Math.sqrt(newY * newY - newX * newX); // y
		
		if (newX > offset + minRadius)
			newX = _master.getX() + newX - offset;
		else
			newX = _master.getX() - newX + minRadius;
		
		if (newY > offset + minRadius)
			newY = _master.getY() + newY - offset;
		else
			newY = _master.getY() - newY + minRadius;
		
		teleToLocation(newX, newY, _master.getZ(), 0);
	}
}