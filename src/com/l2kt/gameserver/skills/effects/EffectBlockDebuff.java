package com.l2kt.gameserver.skills.effects;

import com.l2kt.gameserver.model.L2Effect;
import com.l2kt.gameserver.skills.Env;
import com.l2kt.gameserver.templates.skills.L2EffectType;

public class EffectBlockDebuff extends L2Effect
{
	public EffectBlockDebuff(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BLOCK_DEBUFF;
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}