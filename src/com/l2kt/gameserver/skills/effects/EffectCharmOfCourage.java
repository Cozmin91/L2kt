package com.l2kt.gameserver.skills.effects;

import com.l2kt.gameserver.model.L2Effect;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.skills.Env;
import com.l2kt.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2kt.gameserver.templates.skills.L2EffectFlag;
import com.l2kt.gameserver.templates.skills.L2EffectType;

/**
 * @author nBd
 */
public class EffectCharmOfCourage extends L2Effect
{
	public EffectCharmOfCourage(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.CHARMOFCOURAGE;
	}
	
	@Override
	public boolean onStart()
	{
		if (getEffected() instanceof Player)
		{
			getEffected().broadcastPacket(new EtcStatusUpdate((Player) getEffected()));
			return true;
		}
		return false;
	}
	
	@Override
	public void onExit()
	{
		if (getEffected() instanceof Player)
			getEffected().broadcastPacket(new EtcStatusUpdate((Player) getEffected()));
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
	
	@Override
	public int getEffectFlags()
	{
		return L2EffectFlag.CHARM_OF_COURAGE.getMask();
	}
}