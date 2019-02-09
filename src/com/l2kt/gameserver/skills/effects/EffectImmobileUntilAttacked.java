package com.l2kt.gameserver.skills.effects;

import com.l2kt.gameserver.model.L2Effect;
import com.l2kt.gameserver.skills.Env;
import com.l2kt.gameserver.templates.skills.L2EffectFlag;
import com.l2kt.gameserver.templates.skills.L2EffectType;

/**
 * @author Ahmed
 */
public class EffectImmobileUntilAttacked extends L2Effect
{
	public EffectImmobileUntilAttacked(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.IMMOBILEUNTILATTACKED;
	}
	
	@Override
	public boolean onStart()
	{
		getEffected().startImmobileUntilAttacked();
		return true;
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopImmobileUntilAttacked(this);
	}
	
	@Override
	public boolean onActionTime()
	{
		getEffected().stopImmobileUntilAttacked(this);
		// just stop this effect
		return false;
	}
	
	@Override
	public int getEffectFlags()
	{
		return L2EffectFlag.MEDITATING.getMask();
	}
}