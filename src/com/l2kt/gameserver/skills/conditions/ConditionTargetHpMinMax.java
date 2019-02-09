package com.l2kt.gameserver.skills.conditions;

import com.l2kt.gameserver.skills.Env;

/**
 * Used for Trap skills.
 * @author Tryskell
 */
public class ConditionTargetHpMinMax extends Condition
{
	private final int _minHp, _maxHp;
	
	public ConditionTargetHpMinMax(int minHp, int maxHp)
	{
		_minHp = minHp;
		_maxHp = maxHp;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (env.getTarget() == null)
			return false;
		
		int _currentHp = (int) env.getTarget().getCurrentHp() * 100 / env.getTarget().getMaxHp();
		return _currentHp >= _minHp && _currentHp <= _maxHp;
	}
}