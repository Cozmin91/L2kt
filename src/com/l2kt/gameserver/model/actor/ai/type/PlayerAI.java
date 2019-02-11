package com.l2kt.gameserver.model.actor.ai.type;

import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.WorldObject;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.Summon;
import com.l2kt.gameserver.model.actor.ai.CtrlIntention;
import com.l2kt.gameserver.model.actor.ai.Desire;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.actor.instance.StaticObject;
import com.l2kt.gameserver.model.location.Location;
import com.l2kt.gameserver.network.serverpackets.ActionFailed;
import com.l2kt.gameserver.network.serverpackets.AutoAttackStart;
import com.l2kt.gameserver.taskmanager.AttackStanceTaskManager;

public class PlayerAI extends PlayableAI
{
	private boolean _thinking; // to prevent recursive thinking
	private Desire _nextIntention = new Desire();
	
	public PlayerAI(Player player)
	{
		super(player);
	}
	
	@Override
	protected void clientActionFailed()
	{
		_actor.sendPacket(ActionFailed.Companion.getSTATIC_PACKET());
	}
	
	@Override
	public Desire getNextIntention()
	{
		return _nextIntention;
	}
	
	/**
	 * Saves the current Intention for this L2PlayerAI if necessary and calls changeIntention in AbstractAI.<BR>
	 * <BR>
	 * @param intention The new Intention to set to the AI
	 * @param arg0 The first parameter of the Intention
	 * @param arg1 The second parameter of the Intention
	 */
	@Override
	synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		// do nothing unless CAST intention
		// however, forget interrupted actions when starting to use an offensive skill
		if (intention != CtrlIntention.CAST || (arg0 != null && ((L2Skill) arg0).isOffensive()))
		{
			_nextIntention.reset();
			super.changeIntention(intention, arg0, arg1);
			return;
		}
		
		// do nothing if next intention is same as current one.
		if (_desire.equals(intention, arg0, arg1))
			return;
		
		// save current intention so it can be used after cast
		_nextIntention.update(_desire);
		
		super.changeIntention(intention, arg0, arg1);
	}
	
	/**
	 * Launch actions corresponding to the Event ReadyToAct.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Launch actions corresponding to the Event Think</li><BR>
	 * <BR>
	 */
	@Override
	protected void onEvtReadyToAct()
	{
		// Launch actions corresponding to the Event Think
		if (!_nextIntention.isBlank())
		{
			setIntention(_nextIntention.getIntention(), _nextIntention.getFirstParameter(), _nextIntention.getSecondParameter());
			_nextIntention.reset();
		}
		super.onEvtReadyToAct();
	}
	
	@Override
	protected void onEvtCancel()
	{
		_nextIntention.reset();
		super.onEvtCancel();
	}
	
	/**
	 * Finalize the casting of a skill. Drop latest intention before the actual CAST.
	 */
	@Override
	protected void onEvtFinishCasting()
	{
		if (_desire.getIntention() == CtrlIntention.CAST)
		{
			if (!_nextIntention.isBlank() && _nextIntention.getIntention() != CtrlIntention.CAST) // previous state shouldn't be casting
				setIntention(_nextIntention.getIntention(), _nextIntention.getFirstParameter(), _nextIntention.getSecondParameter());
			else
				setIntention(CtrlIntention.IDLE);
		}
	}
	
	@Override
	protected void onIntentionRest()
	{
		if (_desire.getIntention() != CtrlIntention.REST)
		{
			changeIntention(CtrlIntention.REST, null, null);
			setTarget(null);
			clientStopMoving(null);
		}
	}
	
	@Override
	protected void onIntentionActive()
	{
		setIntention(CtrlIntention.IDLE);
	}
	
	/**
	 * Manage the Move To Intention : Stop current Attack and Launch a Move to Location Task.<BR>
	 * <BR>
	 * <B><U> Actions</U> : </B><BR>
	 * <BR>
	 * <li>Stop the actor auto-attack server side AND client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Set the Intention of this AI to MOVE_TO</li>
	 * <li>Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet MoveToLocation (broadcast)</li><BR>
	 * <BR>
	 */
	@Override
	protected void onIntentionMoveTo(Location loc)
	{
		// Deny the action if we are currently resting.
		if (_desire.getIntention() == CtrlIntention.REST)
		{
			clientActionFailed();
			return;
		}
		
		// We delay MOVE_TO intention if character is disabled or is currently casting/attacking.
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow() || _actor.isAttackingNow())
		{
			clientActionFailed();
			_nextIntention.update(CtrlIntention.MOVE_TO, loc, null);
			return;
		}
		
		// Set the Intention of this AbstractAI to MOVE_TO
		changeIntention(CtrlIntention.MOVE_TO, loc, null);
		
		// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet MoveToLocation (broadcast)
		moveTo(loc.getX(), loc.getY(), loc.getZ());
	}
	
	@Override
	protected void onIntentionInteract(WorldObject object)
	{
		// Deny the action if we are currently resting.
		if (_desire.getIntention() == CtrlIntention.REST)
		{
			clientActionFailed();
			return;
		}
		
		// We delay INTERACT intention if character is disabled or is currently casting.
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			clientActionFailed();
			_nextIntention.update(CtrlIntention.INTERACT, object, null);
			return;
		}
		
		// Set the Intention of this AbstractAI to INTERACT
		changeIntention(CtrlIntention.INTERACT, object, null);
		
		// Set the AI interact target
		setTarget(object);
		
		// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
		moveToPawn(object, 60);
	}
	
	@Override
	protected void clientNotifyDead()
	{
		_clientMovingToPawnOffset = 0;
		_clientMoving = false;
		
		super.clientNotifyDead();
	}
	
	@Override
	public void startAttackStance()
	{
		if (!AttackStanceTaskManager.INSTANCE.isInAttackStance(_actor))
		{
			final Summon summon = ((Player) _actor).getPet();
			if (summon != null)
				summon.broadcastPacket(new AutoAttackStart(summon.getObjectId()));
			
			_actor.broadcastPacket(new AutoAttackStart(_actor.getObjectId()));
		}
		AttackStanceTaskManager.INSTANCE.add(_actor);
	}
	
	private void thinkAttack()
	{
		final Creature target = (Creature) getTarget();
		if (target == null)
		{
			setTarget(null);
			setIntention(CtrlIntention.ACTIVE);
			return;
		}
		
		if (maybeMoveToPawn(target, _actor.getPhysicalAttackRange()))
			return;
		
		if (target.isAlikeDead())
		{
			if (target instanceof Player && ((Player) target).isFakeDeath())
				target.stopFakeDeath(true);
			else
			{
				setIntention(CtrlIntention.ACTIVE);
				return;
			}
		}
		
		clientStopMoving(null);
		_actor.doAttack(target);
	}
	
	private void thinkCast()
	{
		Creature target = (Creature) getTarget();
		
		if (_skill.getTargetType() == L2Skill.SkillTargetType.TARGET_GROUND && _actor instanceof Player)
		{
			if (maybeMoveToPosition(((Player) _actor).getCurrentSkillWorldPosition(), _skill.getCastRange()))
			{
				_actor.setIsCastingNow(false);
				return;
			}
		}
		else
		{
			if (checkTargetLost(target))
			{
				// Notify the target
				if (_skill.isOffensive() && getTarget() != null)
					setTarget(null);
				
				_actor.setIsCastingNow(false);
				return;
			}
			
			if (target != null && maybeMoveToPawn(target, _skill.getCastRange()))
			{
				_actor.setIsCastingNow(false);
				return;
			}
		}
		
		if (_skill.getHitTime() > 50 && !_skill.isSimultaneousCast())
			clientStopMoving(null);
		
		_actor.doCast(_skill);
	}
	
	private void thinkPickUp()
	{
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow() || _actor.isAttackingNow())
			return;
		
		final WorldObject target = getTarget();
		if (checkTargetLost(target))
			return;
		
		if (maybeMoveToPawn(target, 36))
			return;
		
		setIntention(CtrlIntention.IDLE);
		_actor.getActingPlayer().doPickupItem(target);
	}
	
	private void thinkInteract()
	{
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
			return;
		
		WorldObject target = getTarget();
		if (checkTargetLost(target))
			return;
		
		if (maybeMoveToPawn(target, 36))
			return;
		
		if (!(target instanceof StaticObject))
			_actor.getActingPlayer().doInteract((Creature) target);
		
		setIntention(CtrlIntention.IDLE);
	}
	
	@Override
	protected void onEvtThink()
	{
		// Check if the actor can't use skills and if a thinking action isn't already in progress
		if (_thinking && _desire.getIntention() != CtrlIntention.CAST) // casting must always continue
			return;
		
		// Start thinking action
		_thinking = true;
		
		try
		{
			// Manage AI thoughts
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
			// Stop thinking action
			_thinking = false;
		}
	}
}