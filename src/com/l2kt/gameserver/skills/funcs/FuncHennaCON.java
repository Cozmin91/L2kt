package com.l2kt.gameserver.skills.funcs;

import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.skills.Env;
import com.l2kt.gameserver.skills.Stats;
import com.l2kt.gameserver.skills.basefuncs.Func;

public class FuncHennaCON extends Func
{
	static final FuncHennaCON _fh_instance = new FuncHennaCON();
	
	public static Func getInstance()
	{
		return _fh_instance;
	}
	
	private FuncHennaCON()
	{
		super(Stats.STAT_CON, 0x10, null, null);
	}
	
	@Override
	public void calc(Env env)
	{
		final Player player = env.getPlayer();
		if (player != null)
			env.addValue(player.getHennaStatCON());
	}
}