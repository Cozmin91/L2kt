package com.l2kt.gameserver.handler.skillhandlers;

import com.l2kt.commons.math.MathUtil;
import com.l2kt.gameserver.handler.ISkillHandler;
import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.WorldObject;
import com.l2kt.gameserver.model.actor.Creature;

import com.l2kt.gameserver.network.serverpackets.ValidateLocation;
import com.l2kt.gameserver.templates.skills.L2SkillType;

public class InstantJump implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.INSTANT_JUMP
	};
	
	@Override
	public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets)
	{
		Creature target = (Creature) targets[0];
		
		int px = target.getX();
		int py = target.getY();
		double ph = MathUtil.convertHeadingToDegree(target.getHeading());
		
		ph += 180;
		
		if (ph > 360)
			ph -= 360;
		
		ph = (Math.PI * ph) / 180;
		
		int x = (int) (px + (25 * Math.cos(ph)));
		int y = (int) (py + (25 * Math.sin(ph)));
		int z = target.getZ();
		
		// Cancel current actions.
		activeChar.stopMove(null);
		activeChar.abortAttack();
		activeChar.abortCast();
		
		// Teleport the actor.
		activeChar.setXYZ(x, y, z);
		activeChar.broadcastPacket(new ValidateLocation(activeChar));
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}