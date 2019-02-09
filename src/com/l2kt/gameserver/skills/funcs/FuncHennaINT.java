package com.l2kt.gameserver.skills.funcs;

import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.skills.Env;
import com.l2kt.gameserver.skills.Stats;
import com.l2kt.gameserver.skills.basefuncs.Func;

public class FuncHennaINT extends Func
{
	static final FuncHennaINT _fh_instance = new FuncHennaINT();
	
	public static Func getInstance()
	{
		return _fh_instance;
	}
	
	private FuncHennaINT()
	{
		super(Stats.STAT_INT, 0x10, null, null);
	}
	
	@Override
	public void calc(Env env)
	{
		final Player player = env.getPlayer();
		if (player != null)
			env.addValue(player.getHennaStatINT());
	}
}