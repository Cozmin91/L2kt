package com.l2kt.gameserver.skills.effects;

import com.l2kt.gameserver.model.L2Effect;
import com.l2kt.gameserver.model.actor.instance.Door;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.skills.Env;
import com.l2kt.gameserver.network.serverpackets.ExRegenMax;
import com.l2kt.gameserver.network.serverpackets.StatusUpdate;
import com.l2kt.gameserver.templates.skills.L2EffectType;

class EffectHealOverTime extends L2Effect
{
	public EffectHealOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.HEAL_OVER_TIME;
	}
	
	@Override
	public boolean onStart()
	{
		// If effected is a player, send a hp regen effect packet.
		if (getEffected() instanceof Player && getTotalCount() > 0 && getPeriod() > 0)
			getEffected().sendPacket(new ExRegenMax(getTotalCount() * getPeriod(), getPeriod(), calc()));
		
		return true;
	}
	
	@Override
	public boolean onActionTime()
	{
		// Doesn't affect doors and dead characters.
		if (getEffected().isDead() || getEffected() instanceof Door)
			return false;
		
		// Retrieve maximum hp.
		final double maxHp = getEffected().getMaxHp();
		
		// Calculate new hp amount. If higher than max, pick max.
		double newHp = getEffected().getCurrentHp() + calc();
		if (newHp > maxHp)
			newHp = maxHp;
		
		// Set hp amount.
		getEffected().setCurrentHp(newHp);
		
		// Send status update.
		final StatusUpdate su = new StatusUpdate(getEffected());
		su.addAttribute(StatusUpdate.CUR_HP, (int) newHp);
		getEffected().sendPacket(su);
		return true;
	}
}