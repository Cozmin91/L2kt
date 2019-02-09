package com.l2kt.gameserver.skills.funcs;

import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.skills.Env;
import com.l2kt.gameserver.skills.Stats;
import com.l2kt.gameserver.skills.basefuncs.Func;

public class FuncHennaSTR extends Func
{
	static final FuncHennaSTR _fh_instance = new FuncHennaSTR();
	
	public static Func getInstance()
	{
		return _fh_instance;
	}
	
	private FuncHennaSTR()
	{
		super(Stats.STAT_STR, 0x10, null, null);
	}
	
	@Override
	public void calc(Env env)
	{
		final Player player = env.getPlayer();
		if (player != null)
			env.addValue(player.getHennaStatSTR());
	}
}