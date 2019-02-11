package com.l2kt.gameserver.handler.skillhandlers;

import com.l2kt.commons.random.Rnd;
import com.l2kt.gameserver.handler.ISkillHandler;
import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.WorldObject;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.ai.CtrlIntention;
import com.l2kt.gameserver.model.actor.instance.Chest;
import com.l2kt.gameserver.model.actor.instance.Door;

import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;
import com.l2kt.gameserver.templates.skills.L2SkillType;

public class Unlock implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.UNLOCK,
		L2SkillType.UNLOCK_SPECIAL
	};
	
	@Override
	public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets)
	{
		final WorldObject object = targets[0];
		
		if (object instanceof Door)
		{
			final Door door = (Door) object;
			if (!door.isUnlockable() && skill.getSkillType() != L2SkillType.UNLOCK_SPECIAL)
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.UNABLE_TO_UNLOCK_DOOR));
				return;
			}
			
			if (doorUnlock(skill) && (!door.isOpened()))
				door.openMe();
			else
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_UNLOCK_DOOR));
		}
		else if (object instanceof Chest)
		{
			final Chest chest = (Chest) object;
			if (chest.isDead() || chest.isInteracted())
				return;
			
			chest.setInteracted();
			if (chestUnlock(skill, chest))
			{
				chest.setSpecialDrop();
				chest.doDie(null);
			}
			else
			{
				chest.addDamageHate(activeChar, 0, 999);
				chest.getAI().setIntention(CtrlIntention.ATTACK, activeChar);
			}
		}
		else
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
	}
	
	private static final boolean doorUnlock(L2Skill skill)
	{
		if (skill.getSkillType() == L2SkillType.UNLOCK_SPECIAL)
			return Rnd.INSTANCE.get(100) < skill.getPower();
		
		switch (skill.getLevel())
		{
			case 0:
				return false;
			case 1:
				return Rnd.INSTANCE.get(120) < 30;
			case 2:
				return Rnd.INSTANCE.get(120) < 50;
			case 3:
				return Rnd.INSTANCE.get(120) < 75;
			default:
				return Rnd.INSTANCE.get(120) < 100;
		}
	}
	
	private static final boolean chestUnlock(L2Skill skill, Creature chest)
	{
		int chance = 0;
		if (chest.getLevel() > 60)
		{
			if (skill.getLevel() < 10)
				return false;
			
			chance = (skill.getLevel() - 10) * 5 + 30;
		}
		else if (chest.getLevel() > 40)
		{
			if (skill.getLevel() < 6)
				return false;
			
			chance = (skill.getLevel() - 6) * 5 + 10;
		}
		else if (chest.getLevel() > 30)
		{
			if (skill.getLevel() < 3)
				return false;
			
			if (skill.getLevel() > 12)
				return true;
			
			chance = (skill.getLevel() - 3) * 5 + 30;
		}
		else
		{
			if (skill.getLevel() > 10)
				return true;
			
			chance = skill.getLevel() * 5 + 35;
		}
		
		chance = Math.min(chance, 50);
		return Rnd.INSTANCE.get(100) < chance;
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}