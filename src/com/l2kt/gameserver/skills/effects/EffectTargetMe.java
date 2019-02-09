package com.l2kt.gameserver.skills.effects;

import com.l2kt.gameserver.model.L2Effect;
import com.l2kt.gameserver.model.actor.ai.CtrlIntention;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.skills.Env;
import com.l2kt.gameserver.templates.skills.L2EffectType;

/**
 * @author -Nemesiss-
 */
public class EffectTargetMe extends L2Effect
{
	public EffectTargetMe(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.TARGET_ME;
	}
	
	@Override
	public boolean onStart()
	{
		// work only on players, cause mobs got their own aggro system (AGGDAMAGE, AGGREMOVE, etc)
		if (getEffected() instanceof Player)
		{
			// add an INTENTION_ATTACK, but only if victim got attacker as target
			if (getEffected().getTarget() == getEffector())
				getEffected().getAI().setIntention(CtrlIntention.ATTACK, getEffector());
			// target the agressor
			else
				getEffected().setTarget(getEffector());
			
			return true;
		}
		return false;
	}
	
	@Override
	public void onExit()
	{
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}