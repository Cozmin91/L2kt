package com.l2kt.gameserver.skills.conditions;

import com.l2kt.gameserver.skills.Env;
import com.l2kt.gameserver.skills.Stats;

/**
 * @author mkizub
 */
public class ConditionSkillStats extends Condition
{
	private final Stats _stat;
	
	public ConditionSkillStats(Stats stat)
	{
		super();
		_stat = stat;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		return env.getSkill() != null && env.getSkill().getStat() == _stat;
	}
}