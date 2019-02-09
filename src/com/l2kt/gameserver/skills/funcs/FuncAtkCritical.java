package com.l2kt.gameserver.skills.funcs;

import com.l2kt.gameserver.model.actor.Summon;
import com.l2kt.gameserver.skills.Env;
import com.l2kt.gameserver.skills.Formulas;
import com.l2kt.gameserver.skills.Stats;
import com.l2kt.gameserver.skills.basefuncs.Func;

public class FuncAtkCritical extends Func
{
	static final FuncAtkCritical _fac_instance = new FuncAtkCritical();
	
	public static Func getInstance()
	{
		return _fac_instance;
	}
	
	private FuncAtkCritical()
	{
		super(Stats.CRITICAL_RATE, 0x09, null, null);
	}
	
	@Override
	public void calc(Env env)
	{
		if (!(env.getCharacter() instanceof Summon))
			env.mulValue(Formulas.DEX_BONUS[env.getCharacter().getDEX()]);
		
		env.mulValue(10);
		
		env.setBaseValue(env.getValue());
	}
}
