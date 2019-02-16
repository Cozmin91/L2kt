package com.l2kt.gameserver.model.actor.instance;

import com.l2kt.gameserver.data.manager.ZoneManager;
import com.l2kt.gameserver.data.xml.NpcData;
import com.l2kt.gameserver.geoengine.GeoEngine;
import com.l2kt.gameserver.model.L2Spawn;
import com.l2kt.gameserver.model.WorldObject;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.Npc;
import com.l2kt.gameserver.model.actor.ai.CtrlIntention;
import com.l2kt.gameserver.model.actor.template.NpcTemplate;
import com.l2kt.gameserver.model.entity.Siege.SiegeSide;
import com.l2kt.gameserver.model.zone.CastleZoneType;
import com.l2kt.gameserver.model.zone.ZoneType;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.ActionFailed;
import com.l2kt.gameserver.network.serverpackets.MoveToPawn;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;

import java.util.List;

public class FlameTower extends Npc
{
	private int _upgradeLevel;
	private List<Integer> _zoneList;
	
	public FlameTower(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public boolean isAttackable()
	{
		// Attackable during siege by attacker only
		return getCastle() != null && getCastle().getSiege().isInProgress();
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		// Attackable during siege by attacker only
		return attacker instanceof Player && getCastle() != null && getCastle().getSiege().isInProgress() && getCastle().getSiege().checkSide(((Player) attacker).getClan(), SiegeSide.ATTACKER);
	}
	
	@Override
	public void onForcedAttack(Player player)
	{
		onAction(player);
	}
	
	@Override
	public void onAction(Player player)
	{
		// Set the target of the player
		if (player.getTarget() != this)
			player.setTarget(this);
		else
		{
			if (isAutoAttackable(player) && Math.abs(player.getZ() - getZ()) < 100 && GeoEngine.INSTANCE.canSeeTarget(player, this))
			{
				// Notify the Player AI with INTERACT
				player.getAI().setIntention(CtrlIntention.ATTACK, this);
			}
			else
			{
				// Stop moving if we're already in interact range.
				if (player.isMoving() || player.isInCombat())
					player.getAI().setIntention(CtrlIntention.IDLE);
				
				// Rotate the player to face the instance
				player.sendPacket(new MoveToPawn(player, this, Npc.INTERACTION_DISTANCE));
				
				// Send ActionFailed to the player in order to avoid he stucks
				player.sendPacket(ActionFailed.Companion.getSTATIC_PACKET());
			}
		}
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		enableZones(false);
		
		if (getCastle() != null)
		{
			// Message occurs only if the trap was triggered first.
			if (_zoneList != null && _upgradeLevel != 0)
				getCastle().getSiege().announceToPlayers(SystemMessage.Companion.getSystemMessage(SystemMessageId.A_TRAP_DEVICE_HAS_BEEN_STOPPED), false);
			
			// Spawn a little version of it. This version is a simple NPC, cleaned on siege end.
			try
			{
				final L2Spawn spawn = new L2Spawn(NpcData.INSTANCE.getTemplate(13005));
				spawn.setLoc(getPosition());
				
				final Npc tower = spawn.doSpawn(false);
				tower.setCastle(getCastle());
				
				getCastle().getSiege().getDestroyedTowers().add(tower);
			}
			catch (Exception e)
			{
				WorldObject.LOGGER.error("Couldn't spawn the flame tower.", e);
			}
		}
		
		return super.doDie(killer);
	}
	
	@Override
	public void deleteMe()
	{
		enableZones(false);
		super.deleteMe();
	}
	
	public final void enableZones(boolean state)
	{
		if (_zoneList != null && _upgradeLevel != 0)
		{
			final int maxIndex = _upgradeLevel * 2;
			for (int i = 0; i < maxIndex; i++)
			{
				final ZoneType zone = ZoneManager.INSTANCE.getZoneById(_zoneList.get(i));
				if (zone != null && zone instanceof CastleZoneType)
					((CastleZoneType) zone).setEnabled(state);
			}
		}
	}
	
	public final void setUpgradeLevel(int level)
	{
		_upgradeLevel = level;
	}
	
	public final void setZoneList(List<Integer> list)
	{
		_zoneList = list;
		enableZones(true);
	}
}