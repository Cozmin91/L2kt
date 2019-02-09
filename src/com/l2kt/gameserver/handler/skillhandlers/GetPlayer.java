package com.l2kt.gameserver.handler.skillhandlers;

import com.l2kt.gameserver.handler.ISkillHandler;
import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.WorldObject;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.network.serverpackets.FlyToLocation;
import com.l2kt.gameserver.network.serverpackets.FlyToLocation.FlyType;
import com.l2kt.gameserver.network.serverpackets.ValidateLocation;
import com.l2kt.gameserver.templates.skills.L2SkillType;

public class GetPlayer implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.GET_PLAYER
	};
	
	@Override
	public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets)
	{
		if (activeChar.isAlikeDead())
			return;
		
		for (WorldObject target : targets)
		{
			final Player victim = target.getActingPlayer();
			if (victim == null || victim.isAlikeDead())
				continue;
			
			// Cancel current actions.
			victim.stopMove(null);
			victim.abortAttack();
			victim.abortCast();
			
			// Teleport the actor.
			victim.broadcastPacket(new FlyToLocation(victim, activeChar, FlyType.DUMMY));
			victim.setXYZ(activeChar.getX(), activeChar.getY(), activeChar.getZ());
			victim.broadcastPacket(new ValidateLocation(victim));
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}