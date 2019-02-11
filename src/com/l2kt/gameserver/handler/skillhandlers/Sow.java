package com.l2kt.gameserver.handler.skillhandlers;

import com.l2kt.commons.random.Rnd;
import com.l2kt.gameserver.handler.ISkillHandler;
import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.WorldObject;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.ai.CtrlIntention;
import com.l2kt.gameserver.model.actor.instance.Monster;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.group.Party;
import com.l2kt.gameserver.model.manor.Seed;

import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.PlaySound;
import com.l2kt.gameserver.scripting.QuestState;
import com.l2kt.gameserver.templates.skills.L2SkillType;

public class Sow implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.SOW
	};
	
	@Override
	public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets)
	{
		if (!(activeChar instanceof Player))
			return;
		
		final WorldObject object = targets[0];
		if (!(object instanceof Monster))
			return;
		
		final Player player = (Player) activeChar;
		final Monster target = (Monster) object;
		
		if (target.isDead() || !target.isSeeded() || target.getSeederId() != activeChar.getObjectId())
			return;
		
		final Seed seed = target.getSeed();
		if (seed == null)
			return;
		
		// Consuming used seed
		if (!activeChar.destroyItemByItemId("Consume", seed.getSeedId(), 1, target, false))
			return;
		
		SystemMessageId smId;
		if (calcSuccess(activeChar, target, seed))
		{
			player.sendPacket(new PlaySound(QuestState.SOUND_ITEMGET));
			target.setSeeded(activeChar.getObjectId());
			smId = SystemMessageId.THE_SEED_WAS_SUCCESSFULLY_SOWN;
		}
		else
			smId = SystemMessageId.THE_SEED_WAS_NOT_SOWN;
		
		final Party party = player.getParty();
		if (party == null)
			player.sendPacket(smId);
		else
			party.broadcastMessage(smId);
		
		target.getAI().setIntention(CtrlIntention.IDLE);
	}
	
	private static boolean calcSuccess(Creature activeChar, Creature target, Seed seed)
	{
		final int minlevelSeed = seed.getLevel() - 5;
		final int maxlevelSeed = seed.getLevel() + 5;
		
		final int levelPlayer = activeChar.getLevel(); // Attacker Level
		final int levelTarget = target.getLevel(); // target Level
		
		int basicSuccess = (seed.isAlternative()) ? 20 : 90;
		
		// Seed level
		if (levelTarget < minlevelSeed)
			basicSuccess -= 5 * (minlevelSeed - levelTarget);
		
		if (levelTarget > maxlevelSeed)
			basicSuccess -= 5 * (levelTarget - maxlevelSeed);
		
		// 5% decrease in chance if player level is more than +/- 5 levels to _target's_ level
		int diff = (levelPlayer - levelTarget);
		if (diff < 0)
			diff = -diff;
		
		if (diff > 5)
			basicSuccess -= 5 * (diff - 5);
		
		// Chance can't be less than 1%
		if (basicSuccess < 1)
			basicSuccess = 1;
		
		return Rnd.INSTANCE.get(99) < basicSuccess;
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}