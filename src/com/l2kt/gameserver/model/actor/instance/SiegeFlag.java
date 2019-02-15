package com.l2kt.gameserver.model.actor.instance;

import com.l2kt.commons.concurrent.ThreadPool;
import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.Npc;
import com.l2kt.gameserver.model.actor.ai.CtrlIntention;

import com.l2kt.gameserver.model.actor.template.NpcTemplate;
import com.l2kt.gameserver.model.pledge.Clan;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.ActionFailed;
import com.l2kt.gameserver.network.serverpackets.MoveToPawn;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;

public class SiegeFlag extends Npc
{
	private final Clan _clan;
	
	public SiegeFlag(Player player, int objectId, NpcTemplate template)
	{
		super(objectId, template);
		
		_clan = player.getClan();
		
		// Player clan became null during flag initialization ; don't bother setting clan flag.
		if (_clan != null)
			_clan.setFlag(this);
	}
	
	@Override
	public boolean isAttackable()
	{
		return true;
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return true;
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
			return false;
		
		// Reset clan flag to null.
		if (_clan != null)
			_clan.setFlag(null);
		
		return true;
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
			if (isAutoAttackable(player) && Math.abs(player.getZ() - getZ()) < 100)
				player.getAI().setIntention(CtrlIntention.ATTACK, this);
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
	public void reduceCurrentHp(double damage, Creature attacker, L2Skill skill)
	{
		// Send warning to owners of headquarters that theirs base is under attack.
		if (_clan != null && isScriptValue(0))
		{
			_clan.broadcastToOnlineMembers(SystemMessage.Companion.getSystemMessage(SystemMessageId.BASE_UNDER_ATTACK));
			
			setScriptValue(1);
			ThreadPool.INSTANCE.schedule(() -> setScriptValue(0), 30000);
		}
		super.reduceCurrentHp(damage, attacker, skill);
	}
	
	@Override
	public void addFuncsToNewCharacter()
	{
	}
}