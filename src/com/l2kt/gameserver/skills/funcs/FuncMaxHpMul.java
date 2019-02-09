package com.l2kt.gameserver.skills.funcs;

import com.l2kt.gameserver.skills.Env;
import com.l2kt.gameserver.skills.Formulas;
import com.l2kt.gameserver.skills.Stats;
import com.l2kt.gameserver.skills.basefuncs.Func;

public class FuncMaxHpMul extends Func
{
	static final FuncMaxHpMul _fmhm_instance = new FuncMaxHpMul();
	
	public static Func getInstance()
	{
		return _fmhm_instance;
	}
	
	private FuncMaxHpMul()
	{
		super(Stats.MAX_HP, 0x20, null, null);
	}
	
	@Override
	public void calc(Env env)
	{
		env.mulValue(Formulas.CON_BONUS[env.getCharacter().getCON()]);
	}
}