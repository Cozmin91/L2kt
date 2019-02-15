package com.l2kt.gameserver.model.actor.instance;

import com.l2kt.commons.random.Rnd;
import com.l2kt.gameserver.data.manager.RaidPointManager;
import com.l2kt.gameserver.model.actor.Creature;

import com.l2kt.gameserver.model.actor.template.NpcTemplate;
import com.l2kt.gameserver.model.entity.Hero;
import com.l2kt.gameserver.model.group.Party;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.PlaySound;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;

/**
 * This class manages all Grand Bosses.
 */
public final class GrandBoss extends Monster
{
	/**
	 * Constructor for L2GrandBossInstance. This represent all grandbosses.
	 * @param objectId ID of the instance
	 * @param template L2NpcTemplate of the instance
	 */
	public GrandBoss(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setRaid(true);
	}
	
	@Override
	public void onSpawn()
	{
		setIsNoRndWalk(true);
		super.onSpawn();
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
			return false;
		
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
		
		return true;
	}
	
	@Override
	public boolean returnHome()
	{
		return false;
	}
}