package com.l2kt.gameserver.model.actor.instance;

import java.util.concurrent.ScheduledFuture;

import com.l2kt.Config;
import com.l2kt.commons.concurrent.ThreadPool;
import com.l2kt.commons.random.Rnd;
import com.l2kt.gameserver.data.manager.RaidPointManager;
import com.l2kt.gameserver.model.L2Spawn;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.ai.type.AttackableAI;

import com.l2kt.gameserver.instancemanager.RaidBossSpawnManager;
import com.l2kt.gameserver.instancemanager.RaidBossSpawnManager.StatusEnum;
import com.l2kt.gameserver.model.actor.template.NpcTemplate;
import com.l2kt.gameserver.model.entity.Hero;
import com.l2kt.gameserver.model.group.Party;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.PlaySound;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;

/**
 * This class manages all classic raid bosses.<br>
 * <br>
 * Raid Bosses (RB) are mobs which are supposed to be defeated by a party of several players. It extends most of {@link Monster} aspects.<br>
 * They automatically teleport if out of their initial spawn area, and can randomly attack a Player from their Hate List once attacked.
 */
public class RaidBoss extends Monster
{
	private StatusEnum _raidStatus;
	private ScheduledFuture<?> _maintenanceTask;
	
	public RaidBoss(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setRaid(true);
	}
	
	@Override
	public void onSpawn()
	{
		// No random walk allowed.
		setIsNoRndWalk(true);
		
		// Basic behavior.
		super.onSpawn();
		
		// "AI task" for regular bosses.
		_maintenanceTask = ThreadPool.INSTANCE.scheduleAtFixedRate(() ->
		{
			// Don't bother with dead bosses.
			if (!isDead())
			{
				// The boss isn't in combat, check the teleport possibility.
				if (!isInCombat())
				{
					// Gordon is excluded too.
					if (getNpcId() != 29095 && Rnd.INSTANCE.nextBoolean())
					{
						// Spawn must exist.
						final L2Spawn spawn = getSpawn();
						if (spawn == null)
							return;
						
						// If the boss is above drift range (or 200 minimum), teleport him on his spawn.
						if (!isInsideRadius(spawn.getLocX(), spawn.getLocY(), spawn.getLocZ(), Math.max(Config.MAX_DRIFT_RANGE, 200), true, false))
							teleToLocation(spawn.getLoc(), 0);
					}
				}
				// Randomized attack if the boss is already attacking.
				else if (Rnd.INSTANCE.get(5) == 0)
					((AttackableAI) getAI()).aggroReconsider();
			}
			
			// For each minion (if any), randomize the attack.
			if (hasMinions())
			{
				for (Monster minion : getMinionList().getSpawnedMinions())
				{
					// Don't bother with dead minions.
					if (minion.isDead() || !minion.isInCombat())
						return;
					
					// Randomized attack if the boss is already attacking.
					if (Rnd.INSTANCE.get(3) == 0)
						((AttackableAI) minion.getAI()).aggroReconsider();
				}
			}
		}, 1000, 60000);
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
			return false;
		
		if (_maintenanceTask != null)
		{
			_maintenanceTask.cancel(false);
			_maintenanceTask = null;
		}
		
		if (killer != null)
		{
			final Player player = killer.getActingPlayer();
			if (player != null)
			{
				broadcastPacket(SystemMessage.Companion.getSystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL));
				broadcastPacket(new PlaySound("systemmsg_e.1209"));
				
				final Party party = player.getParty();
				if (party != null)
				{
					for (Player member : party.getMembers())
					{
						RaidPointManager.INSTANCE.addPoints(member, getNpcId(), (getLevel() / 2) + Rnd.INSTANCE.get(-5, 5));
						if (member.isNoble())
							Hero.getInstance().setRBkilled(member.getObjectId(), getNpcId());
					}
				}
				else
				{
					RaidPointManager.INSTANCE.addPoints(player, getNpcId(), (getLevel() / 2) + Rnd.INSTANCE.get(-5, 5));
					if (player.isNoble())
						Hero.getInstance().setRBkilled(player.getObjectId(), getNpcId());
				}
			}
		}
		
		RaidBossSpawnManager.INSTANCE.updateStatus(this, true);
		return true;
	}
	
	@Override
	public void deleteMe()
	{
		if (_maintenanceTask != null)
		{
			_maintenanceTask.cancel(false);
			_maintenanceTask = null;
		}
		
		super.deleteMe();
	}
	
	public StatusEnum getRaidStatus()
	{
		return _raidStatus;
	}
	
	public void setRaidStatus(StatusEnum status)
	{
		_raidStatus = status;
	}
}