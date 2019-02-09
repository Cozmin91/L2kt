package com.l2kt.gameserver.skills.funcs;

import com.l2kt.gameserver.skills.Env;
import com.l2kt.gameserver.skills.Formulas;
import com.l2kt.gameserver.skills.Stats;
import com.l2kt.gameserver.skills.basefuncs.Func;

public class FuncMAtkSpeed extends Func
{
	static final FuncMAtkSpeed _fas_instance = new FuncMAtkSpeed();
	
	public static Func getInstance()
	{
		return _fas_instance;
	}
	
	private FuncMAtkSpeed()
	{
		super(Stats.MAGIC_ATTACK_SPEED, 0x20, null, null);
	}
	
	@Override
	public void calc(Env env)
	{
		env.mulValue(Formulas.WIT_BONUS[env.getCharacter().getWIT()]);
	}
}