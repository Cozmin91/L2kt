package com.l2kt.gameserver.skills.conditions;

import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.skills.Env;

/**
 * @author mkizub
 */
public class ConditionPlayerState extends Condition
{
	public enum PlayerState
	{
		RESTING,
		MOVING,
		RUNNING,
		RIDING,
		FLYING,
		BEHIND,
		FRONT,
		OLYMPIAD
	}
	
	private final PlayerState _check;
	private final boolean _required;
	
	public ConditionPlayerState(PlayerState check, boolean required)
	{
		_check = check;
		_required = required;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		final Creature character = env.getCharacter();
		final Player player = env.getPlayer();
		
		switch (_check)
		{
			case RESTING:
				return (player == null) ? !_required : player.isSitting() == _required;
			
			case MOVING:
				return character.isMoving() == _required;
			
			case RUNNING:
				return character.isMoving() == _required && character.isRunning() == _required;
			
			case RIDING:
				return character.isRiding() == _required;
			
			case FLYING:
				return character.isFlying() == _required;
			
			case BEHIND:
				return character.isBehindTarget() == _required;
			
			case FRONT:
				return character.isInFrontOfTarget() == _required;
			
			case OLYMPIAD:
				return (player == null) ? !_required : player.isInOlympiadMode() == _required;
		}
		return !_required;
	}
}