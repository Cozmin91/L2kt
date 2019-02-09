package com.l2kt.gameserver.skills.effects;

import com.l2kt.gameserver.model.L2Effect;
import com.l2kt.gameserver.skills.Env;
import com.l2kt.gameserver.templates.skills.L2EffectFlag;
import com.l2kt.gameserver.templates.skills.L2EffectType;

public class EffectStunSelf extends L2Effect
{
	public EffectStunSelf(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.STUN_SELF;
	}
	
	@Override
	public boolean onStart()
	{
		getEffector().startStunning();
		return true;
	}
	
	@Override
	public void onExit()
	{
		getEffector().stopStunning(false);
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
	
	@Override
	public boolean isSelfEffectType()
	{
		return true;
	}
	
	@Override
	public int getEffectFlags()
	{
		return L2EffectFlag.STUNNED.getMask();
	}
}