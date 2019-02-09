package com.l2kt.gameserver.skills.conditions;

import com.l2kt.gameserver.skills.Env;

/**
 * @author mr
 */
public class ConditionPlayerHp extends Condition
{
	private final int _hp;
	
	public ConditionPlayerHp(int hp)
	{
		_hp = hp;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		return env.getCharacter().getCurrentHp() * 100 / env.getCharacter().getMaxHp() <= _hp;
	}
}