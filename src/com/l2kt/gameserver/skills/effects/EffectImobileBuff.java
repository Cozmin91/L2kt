package com.l2kt.gameserver.skills.effects;

import com.l2kt.gameserver.model.L2Effect;
import com.l2kt.gameserver.skills.Env;
import com.l2kt.gameserver.templates.skills.L2EffectType;

/**
 * @author mkizub
 */
final class EffectImobileBuff extends L2Effect
{
	
	public EffectImobileBuff(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BUFF;
	}
	
	@Override
	public boolean onStart()
	{
		getEffector().setIsImmobilized(true);
		return true;
	}
	
	@Override
	public void onExit()
	{
		getEffector().setIsImmobilized(false);
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}