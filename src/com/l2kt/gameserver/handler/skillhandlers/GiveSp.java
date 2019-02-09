package com.l2kt.gameserver.handler.skillhandlers;

import com.l2kt.gameserver.handler.ISkillHandler;
import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.WorldObject;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.templates.skills.L2SkillType;

public class GiveSp implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.GIVE_SP
	};
	
	@Override
	public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets)
	{
		final int spToAdd = (int) skill.getPower();
		
		for (WorldObject obj : targets)
		{
			final Creature target = (Creature) obj;
			if (target != null)
				target.addExpAndSp(0, spToAdd);
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}