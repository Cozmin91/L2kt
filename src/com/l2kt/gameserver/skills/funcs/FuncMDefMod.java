package com.l2kt.gameserver.skills.funcs;

import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.itemcontainer.Inventory;
import com.l2kt.gameserver.skills.Env;
import com.l2kt.gameserver.skills.Formulas;
import com.l2kt.gameserver.skills.Stats;
import com.l2kt.gameserver.skills.basefuncs.Func;

public class FuncMDefMod extends Func
{
	static final FuncMDefMod _fpa_instance = new FuncMDefMod();
	
	public static Func getInstance()
	{
		return _fpa_instance;
	}
	
	private FuncMDefMod()
	{
		super(Stats.MAGIC_DEFENCE, 0x20, null, null);
	}
	
	@Override
	public void calc(Env env)
	{
		if (env.getCharacter() instanceof Player)
		{
			final Player player = env.getPlayer();
			if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER) != null)
				env.subValue(5);
			if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RFINGER) != null)
				env.subValue(5);
			if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR) != null)
				env.subValue(9);
			if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_REAR) != null)
				env.subValue(9);
			if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK) != null)
				env.subValue(13);
		}
		
		env.mulValue(Formulas.MEN_BONUS[env.getCharacter().getMEN()] * env.getCharacter().getLevelMod());
	}
}