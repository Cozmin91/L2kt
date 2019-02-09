package com.l2kt.gameserver.skills.conditions;

import com.l2kt.gameserver.skills.Env;

/**
 * The Class ConditionPlayerSex.
 */
public class ConditionPlayerSex extends Condition
{
	private final int _sex;
	
	/**
	 * Instantiates a new condition player sex.
	 * @param sex the sex
	 */
	public ConditionPlayerSex(int sex)
	{
		_sex = sex;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (env.getPlayer() == null)
			return false;
		
		return env.getPlayer().getAppearance().getSex().ordinal() == _sex;
	}
}