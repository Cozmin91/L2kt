package com.l2kt.gameserver.model.actor.ai.type;

import com.l2kt.commons.random.Rnd;
import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.WorldObject;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.Summon;
import com.l2kt.gameserver.model.actor.ai.CtrlIntention;

import com.l2kt.gameserver.geoengine.GeoEngine;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.taskmanager.AttackStanceTaskManager;

public class SummonAI extends PlayableAI
{
	private static final int AVOID_RADIUS = 70;
	
	private volatile boolean _thinking; // to prevent recursive thinking
	private volatile boolean _startFollow = ((Summon) _actor).getFollowStatus();
	private Creature _lastAttack = null;
	
	public SummonAI(Summon summon)
	{
		super(summon);
	}
	
	@Override
	protected void onIntentionIdle()
	{
		stopFollow();
		_startFollow = false;
		onIntentionActive();
	}
	
	@Override
	protected void onIntentionActive()
	{
		Summon summon = (Summon) _actor;
		if (_startFollow)
			setIntention(CtrlIntention.FOLLOW, summon.getOwner());
		else
			super.onIntentionActive();
	}
	
	private void thinkAttack()
	{
		final Creature target = (Creature) getTarget();
		
		if (checkTargetLostOrDead(target))
		{
			setTarget(null);
			return;
		}
		
		if (maybeMoveToPawn(target, _actor.getPhysicalAttackRange()))
			return;
		
		clientStopMoving(null);
		_actor.doAttack(target);
	}
	
	private void thinkCast()
	{
		final WorldObject target = getTarget();
		if (checkTargetLost(target))
		{
			setTarget(null);
			return;
		}
		
		boolean val = _startFollow;
		if (maybeMoveToPawn(target, _skill.getCastRange()))
			return;
		
		clientStopMoving(null);
		((Summon) _actor).setFollowStatus(false);
		setIntention(CtrlIntention.IDLE);
		
		_startFollow = val;
		_actor.doCast(_skill);
	}
	
	private void thinkPickUp()
	{
		final WorldObject target = getTarget();
		if (checkTargetLost(target))
			return;
		
		if (maybeMoveToPawn(target, 36))
			return;
		
		setIntention(CtrlIntention.IDLE);
		((Summon) _actor).doPickupItem(target);
	}
	
	private void thinkInteract()
	{
		final WorldObject target = getTarget();
		if (checkTargetLost(target))
			return;
		
		if (maybeMoveToPawn(target, 36))
			return;
		
		setIntention(CtrlIntention.IDLE);
	}
	
	@Override
	protected void onEvtThink()
	{
		if (_thinking || _actor.isCastingNow() || _actor.isAllSkillsDisabled())
			return;
		
		_thinking = true;
		try
		{
			switch (_desire.getIntention())
			{
				case ATTACK:
					thinkAttack();
					break;
				case CAST:
					thinkCast();
					break;
				case PICK_UP:
					thinkPickUp();
					break;
				case INTERACT:
					thinkInteract();
					break;
			}
		}
		finally
		{
			_thinking = false;
		}
	}
	
	@Override
	protected void onEvtFinishCasting()
	{
		if (_lastAttack == null)
			((Summon) _actor).setFollowStatus(_startFollow);
		else
		{
			setIntention(CtrlIntention.ATTACK, _lastAttack);
			_lastAttack = null;
		}
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker)
	{
		super.onEvtAttacked(attacker);
		
		avoidAttack(attacker);
	}
	
	@Override
	protected void onEvtEvaded(Creature attacker)
	{
		super.onEvtEvaded(attacker);
		
		avoidAttack(attacker);
	}
	
	@Override
	public void startAttackStance()
	{
		_actor.getActingPlayer().getAI().startAttackStance();
	}
	
	private void avoidAttack(Creature attacker)
	{
		final Player owner = ((Summon) _actor).getOwner();
		
		// Must have a owner, the attacker can't be the owner and the owner must be in a short radius. The owner must be under attack stance (the summon CAN'T be under attack stance with current writing style).
		if (owner == null || owner == attacker || !owner.isInsideRadius(_actor, 2 * AVOID_RADIUS, true, false) || !AttackStanceTaskManager.INSTANCE.isInAttackStance(owner))
			return;
		
		// Current summon intention must be ACTIVE or FOLLOW type.
		if (_desire.getIntention() != CtrlIntention.ACTIVE && _desire.getIntention() != CtrlIntention.FOLLOW)
			return;
		
		// Summon mustn't be under movement, must be alive and not be movement disabled.
		if (_clientMoving || _actor.isDead() || _actor.isMovementDisabled())
			return;
		
		final int ownerX = owner.getX();
		final int ownerY = owner.getY();
		final double angle = Math.toRadians(Rnd.INSTANCE.get(-90, 90)) + Math.atan2(ownerY - _actor.getY(), ownerX - _actor.getX());
		
		final int targetX = ownerX + (int) (AVOID_RADIUS * Math.cos(angle));
		final int targetY = ownerY + (int) (AVOID_RADIUS * Math.sin(angle));
		
		// If the location is valid, move the summon.
		if (GeoEngine.INSTANCE.canMoveToTarget(_actor.getX(), _actor.getY(), _actor.getZ(), targetX, targetY, _actor.getZ()))
			moveTo(targetX, targetY, _actor.getZ());
	}
	
	public void notifyFollowStatusChange()
	{
		_startFollow = !_startFollow;
		switch (_desire.getIntention())
		{
			case ACTIVE:
			case FOLLOW:
			case IDLE:
			case MOVE_TO:
			case PICK_UP:
				((Summon) _actor).setFollowStatus(_startFollow);
		}
	}
	
	public void setStartFollowController(boolean val)
	{
		_startFollow = val;
	}
	
	@Override
	protected void onIntentionCast(L2Skill skill, WorldObject target)
	{
		if (_desire.getIntention() == CtrlIntention.ATTACK)
			_lastAttack = (Creature) getTarget();
		else
			_lastAttack = null;
		
		super.onIntentionCast(skill, target);
	}
}