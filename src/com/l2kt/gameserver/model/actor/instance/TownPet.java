package com.l2kt.gameserver.model.actor.instance;

import java.util.concurrent.ScheduledFuture;

import com.l2kt.commons.concurrent.ThreadPool;
import com.l2kt.commons.random.Rnd;
import com.l2kt.gameserver.model.actor.Npc;
import com.l2kt.gameserver.model.actor.ai.CtrlIntention;

import com.l2kt.gameserver.geoengine.GeoEngine;
import com.l2kt.gameserver.model.actor.template.NpcTemplate;
import com.l2kt.gameserver.network.serverpackets.ActionFailed;
import com.l2kt.gameserver.network.serverpackets.MoveToPawn;

public class TownPet extends Folk
{
	private ScheduledFuture<?> _aiTask;
	
	public TownPet(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setRunning();
		
		_aiTask = ThreadPool.INSTANCE.scheduleAtFixedRate(new RandomWalkTask(), 1000, 10000);
	}
	
	@Override
	public void onAction(Player player)
	{
		// Set the target of the player
		if (player.getTarget() != this)
			player.setTarget(this);
		else
		{
			if (!canInteract(player))
				player.getAi().setIntention(CtrlIntention.INTERACT, this);
			else
			{
				// Stop moving if we're already in interact range.
				if (player.isMoving() || player.isInCombat())
					player.getAi().setIntention(CtrlIntention.IDLE);
				
				// Rotate the player to face the instance
				player.sendPacket(new MoveToPawn(player, this, Npc.INTERACTION_DISTANCE));
				
				// Send ActionFailed to the player in order to avoid he stucks
				player.sendPacket(ActionFailed.Companion.getSTATIC_PACKET());
			}
		}
	}
	
	@Override
	public void deleteMe()
	{
		if (_aiTask != null)
		{
			_aiTask.cancel(true);
			_aiTask = null;
		}
		super.deleteMe();
	}
	
	public class RandomWalkTask implements Runnable
	{
		@Override
		public void run()
		{
			if (getSpawn() == null)
				return;
			
			getAi().setIntention(CtrlIntention.MOVE_TO, GeoEngine.INSTANCE.canMoveToTargetLoc(getX(), getY(), getZ(), getSpawn().getLocX() + Rnd.INSTANCE.get(-75, 75), getSpawn().getLocY() + Rnd.INSTANCE.get(-75, 75), getZ()));
		}
	}
}