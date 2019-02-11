package com.l2kt.gameserver.handler.skillhandlers;

import com.l2kt.gameserver.handler.ISkillHandler;
import com.l2kt.gameserver.handler.SkillHandler;
import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.ShotType;
import com.l2kt.gameserver.model.WorldObject;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.Npc;
import com.l2kt.gameserver.model.actor.Summon;
import com.l2kt.gameserver.model.actor.instance.Door;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.actor.instance.SiegeFlag;
import com.l2kt.gameserver.skills.Stats;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.StatusUpdate;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;
import com.l2kt.gameserver.templates.skills.L2SkillType;

public class Heal implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.HEAL,
		L2SkillType.HEAL_STATIC
	};
	
	@Override
	public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets)
	{
		final ISkillHandler handler = SkillHandler.getInstance().getHandler(L2SkillType.BUFF);
		if (handler != null)
			handler.useSkill(activeChar, skill, targets);
		
		double power = skill.getPower() + activeChar.calcStat(Stats.HEAL_PROFICIENCY, 0, null, null);
		
		final boolean sps = activeChar.isChargedShot(ShotType.SPIRITSHOT);
		final boolean bsps = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT);
		
		switch (skill.getSkillType())
		{
			case HEAL_STATIC:
				break;
			
			default:
				double staticShotBonus = 0;
				int mAtkMul = 1; // mAtk multiplier
				
				if ((sps || bsps) && (activeChar instanceof Player && activeChar.getActingPlayer().isMageClass()) || activeChar instanceof Summon)
				{
					staticShotBonus = skill.getMpConsume(); // static bonus for spiritshots
					
					if (bsps)
					{
						mAtkMul = 4;
						staticShotBonus *= 2.4;
					}
					else
						mAtkMul = 2;
				}
				else if ((sps || bsps) && activeChar instanceof Npc)
				{
					staticShotBonus = 2.4 * skill.getMpConsume(); // always blessed spiritshots
					mAtkMul = 4;
				}
				else
				{
					// shot dynamic bonus
					if (bsps)
						mAtkMul *= 4;
					else
						mAtkMul += 1;
				}
				
				power += staticShotBonus + Math.sqrt(mAtkMul * activeChar.getMAtk(activeChar, null));
				
				if (!skill.isPotion())
					activeChar.setChargedShot(bsps ? ShotType.BLESSED_SPIRITSHOT : ShotType.SPIRITSHOT, skill.isStaticReuse());
		}
		
		double hp;
		for (WorldObject obj : targets)
		{
			if (!(obj instanceof Creature))
				continue;
			
			final Creature target = ((Creature) obj);
			if (target.isDead() || target.isInvul())
				continue;
			
			if (target instanceof Door || target instanceof SiegeFlag)
				continue;
			
			// Player holding a cursed weapon can't be healed and can't heal
			if (target != activeChar)
			{
				if (target instanceof Player && ((Player) target).isCursedWeaponEquipped())
					continue;
				else if (activeChar instanceof Player && ((Player) activeChar).isCursedWeaponEquipped())
					continue;
			}
			
			switch (skill.getSkillType())
			{
				case HEAL_PERCENT:
					hp = target.getMaxHp() * power / 100.0;
					break;
				default:
					hp = power;
					hp *= target.calcStat(Stats.HEAL_EFFECTIVNESS, 100, null, null) / 100;
			}
			
			// If you have full HP and you get HP buff, u will receive 0HP restored message
			if ((target.getCurrentHp() + hp) >= target.getMaxHp())
				hp = target.getMaxHp() - target.getCurrentHp();
			
			if (hp < 0)
				hp = 0;
			
			target.setCurrentHp(hp + target.getCurrentHp());
			StatusUpdate su = new StatusUpdate(target);
			su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
			target.sendPacket(su);
			
			if (target instanceof Player)
			{
				if (skill.getId() == 4051)
					target.sendPacket(SystemMessage.Companion.getSystemMessage(SystemMessageId.REJUVENATING_HP));
				else
				{
					if (activeChar instanceof Player && activeChar != target)
						target.sendPacket(SystemMessage.Companion.getSystemMessage(SystemMessageId.S2_HP_RESTORED_BY_S1).addCharName(activeChar).addNumber((int) hp));
					else
						target.sendPacket(SystemMessage.Companion.getSystemMessage(SystemMessageId.S1_HP_RESTORED).addNumber((int) hp));
				}
			}
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}